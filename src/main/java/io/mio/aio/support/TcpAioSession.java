package io.mio.aio.support;

import io.mio.aio.AioConstants;
import io.mio.aio.EventState;
import io.mio.aio.MessageProcessor;
import io.mio.aio.NetFilter;
import io.mio.aio.buffer.BufferPage;
import io.mio.aio.buffer.VirtualBuffer;
import io.mio.aio.handler.ReadCompletionHandler;
import io.mio.aio.handler.WriteCompletionHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * AIO传输层会话。
 * <p>
 * AioSession为mio-aio最核心的类，封装{@link AsynchronousSocketChannel} API接口，简化IO操作。
 *
 * @author lry
 */
@Slf4j
public class TcpAioSession<T> {

    /**
     * Session状态:已关闭
     */
    private static final byte SESSION_STATUS_CLOSED = 1;
    /**
     * Session状态:关闭中
     */
    private static final byte SESSION_STATUS_CLOSING = 2;
    /**
     * Session状态:正常
     */
    private static final byte SESSION_STATUS_ENABLED = 3;
    /**
     * 会话当前状态
     *
     * @see TcpAioSession#SESSION_STATUS_CLOSED
     * @see TcpAioSession#SESSION_STATUS_CLOSING
     * @see TcpAioSession#SESSION_STATUS_ENABLED
     */
    private byte status = SESSION_STATUS_ENABLED;

    /**
     * 底层通信channel对象
     */
    protected AsynchronousSocketChannel channel;
    /**
     * 读缓冲。
     * <p>大小取决于AioMioClient/AioMioServer设置的setReadBufferSize</p>
     */
    private VirtualBuffer readBuffer;
    private VirtualBuffer writeBuffer;

    /**
     * 读回调信号量
     */
    private Semaphore readSemaphore;

    /**
     * 输出信号量,防止并发write导致异常
     */
    private Semaphore semaphore = new Semaphore(1);

    private ReadCompletionHandler<T> readCompletionHandler;
    private WriteCompletionHandler<T> writeCompletionHandler;

    private IoServerConfig<T> ioServerConfig;
    /**
     * 输出流
     */
    private WriteBuffer byteBuf;
    /**
     * 是否处于数据输出中
     */
    private boolean writing = false;
    /**
     * 最近一次读取到的字节数
     */
    private int lastReadSize;

    /**
     * 数据输出Function
     */
    private Function<WriteBuffer, Void> flushFunction = new Function<WriteBuffer, Void>() {
        @Override
        public Void apply(WriteBuffer var) {
            if (!semaphore.tryAcquire()) {
                return null;
            }
            TcpAioSession.this.writeBuffer = var.poll();
            if (writeBuffer == null) {
                semaphore.release();
            } else {
                writing = true;
                continueWrite(writeBuffer);
            }
            return null;
        }
    };

    /**
     * 数据快速输出
     */
    private FasterWrite fasterWrite = new FasterWrite() {
        @Override
        public boolean tryAcquire() {
            if (writing) {
                return false;
            }
            return semaphore.tryAcquire();
        }

        @Override
        public void write(VirtualBuffer buffer) {
            writing = true;
            writeBuffer = buffer;
            continueWrite(writeBuffer);
        }
    };

    /**
     * @param channel
     * @param config
     * @param readCompletionHandler
     * @param writeCompletionHandler
     * @param bufferPage             是否服务端Session
     */
    public TcpAioSession(AsynchronousSocketChannel channel,
                         final IoServerConfig<T> config,
                         ReadCompletionHandler<T> readCompletionHandler,
                         WriteCompletionHandler<T> writeCompletionHandler,
                         BufferPage bufferPage) {
        this.channel = channel;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;
        this.ioServerConfig = config;

        this.readBuffer = bufferPage.allocate(config.getReadBufferSize());
        byteBuf = new WriteBuffer(bufferPage, flushFunction, ioServerConfig, fasterWrite);
        //触发状态机
        config.getProcessor().stateEvent(this, EventState.NEW_SESSION, null);
    }

    /**
     * 初始化AioSession
     */
    public void initSession() {
        continueRead();
    }

    /**
     * 触发AIO的写操作,
     * <p>需要调用控制同步</p>
     */
    public void writeToChannel() {
        if (writeBuffer == null) {
            writeBuffer = byteBuf.poll();
        } else if (!writeBuffer.buffer().hasRemaining()) {
            writeBuffer.clean();
            writeBuffer = byteBuf.poll();
        }

        if (writeBuffer != null) {
            continueWrite(writeBuffer);
            return;
        }
        writing = false;
        semaphore.release();
        //此时可能是Closing或Closed状态
        if (status != SESSION_STATUS_ENABLED) {
            close(true);
        } else {
            //也许此时有新的消息通过write方法添加到writeCacheQueue中
            byteBuf.flush();
        }
    }

    public void setReadSemaphore(Semaphore readSemaphore) {
        this.readSemaphore = readSemaphore;
    }

    /**
     * 内部方法：触发通道的读操作
     *
     * @param buffer 用于存放待读取数据的buffer
     */
    protected final void readFromChannel0(ByteBuffer buffer) {
        channel.read(buffer, this, readCompletionHandler);
    }

    /**
     * 内部方法：触发通道的写操作
     *
     * @param buffer 待输出的buffer
     */
    protected final void writeToChannel0(ByteBuffer buffer) {
        channel.write(buffer, 0L, TimeUnit.MILLISECONDS, this, writeCompletionHandler);
    }

    /**
     * @return 输入流
     */
    public final WriteBuffer writeBuffer() {
        return byteBuf;
    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public synchronized void close(boolean immediate) {
        //status == SESSION_STATUS_CLOSED说明close方法被重复调用
        if (status == SESSION_STATUS_CLOSED) {
            log.warn("ignore, session:{} is closed:", getSessionId());
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        boolean noWriteBuffer = (writeBuffer == null || !writeBuffer.buffer().hasRemaining());
        if (immediate) {
            byteBuf.close();
            byteBuf = null;
            readBuffer.clean();
            readBuffer = null;
            if (writeBuffer != null) {
                writeBuffer.clean();
                writeBuffer = null;
            }
            AioConstants.close(channel);
            ioServerConfig.getProcessor().stateEvent(this, EventState.SESSION_CLOSED, null);
        } else if (noWriteBuffer && !byteBuf.hasData()) {
            close(true);
        } else {
            ioServerConfig.getProcessor().stateEvent(this, EventState.SESSION_CLOSING, null);
            byteBuf.flush();
        }
    }

    /**
     * 获取当前Session的唯一标识
     *
     * @return sessionId
     */
    public final String getSessionId() {
        return "aioSession-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     *
     * @return 是否失效
     */
    public final boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }


    /**
     * 触发通道的读回调操作
     *
     * @param eof 输入流是否已关闭
     */
    public void readFromChannel(boolean eof) {
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        final ByteBuffer readBuffer = this.readBuffer.buffer();
        readBuffer.flip();
        final MessageProcessor<T> messageProcessor = ioServerConfig.getProcessor();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            T dataEntry = null;
            try {
                dataEntry = ioServerConfig.getProtocol().decode(readBuffer, this);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, EventState.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            //处理消息
            try {
                messageProcessor.process(this, dataEntry);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, EventState.PROCESS_EXCEPTION, e);
            }
        }

        if (readSemaphore != null) {
            readSemaphore.release();
            readSemaphore = null;
        }


        if (eof || status == SESSION_STATUS_CLOSING) {
            close(false);
            messageProcessor.stateEvent(this, EventState.INPUT_SHUTDOWN, null);
            return;
        }
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }

        if (!writing && byteBuf != null) {
            byteBuf.flush();
        }

        //数据读取完毕
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {
            // 仅当发生数据读取时调用compact,减少内存拷贝
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }

        //读缓冲区已满
        if (!readBuffer.hasRemaining()) {
            RuntimeException exception = new RuntimeException("readBuffer has no remaining");
            messageProcessor.stateEvent(this, EventState.DECODE_EXCEPTION, exception);
            throw exception;
        }

        continueRead();
    }

    /**
     * 触发读操作
     */
    protected void continueRead() {
        NetFilter<T> monitor = getServerConfig().getMonitor();
        if (monitor != null) {
            monitor.beforeRead(this);
        }
        readFromChannel0(readBuffer.buffer());
    }

    /**
     * 同步读取数据
     *
     * @return
     * @throws Exception
     */
    private int synRead() throws IOException {
        ByteBuffer buffer = readBuffer.buffer();
        if (buffer.remaining() > 0) {
            return 0;
        }
        try {
            buffer.clear();
            int size = channel.read(buffer).get();
            buffer.flip();
            return size;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * 触发写操作
     *
     * @param writeBuffer 存放待输出数据的buffer
     */
    protected void continueWrite(VirtualBuffer writeBuffer) {
        NetFilter<T> monitor = getServerConfig().getMonitor();
        if (monitor != null) {
            monitor.beforeWrite(this);
        }
        writeToChannel0(writeBuffer.buffer());
    }

    public int getLastReadSize() {
        return lastReadSize;
    }

    public void setLastReadSize(int lastReadSize) {
        this.lastReadSize = lastReadSize;
    }

    /**
     * @return 本地地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public final InetSocketAddress getLocalAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * @return 远程地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public final InetSocketAddress getRemoteAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * 断言当前会话是否可用
     *
     * @throws IOException IO异常
     */
    private void assertChannel() throws IOException {
        if (status == SESSION_STATUS_CLOSED || channel == null) {
            throw new IOException("session is closed");
        }
    }

    public IoServerConfig<T> getServerConfig() {
        return this.ioServerConfig;
    }

}
