package io.mio.transport.aio.support;

import io.mio.transport.aio.MessageProcessor;
import io.mio.transport.aio.Protocol;
import io.mio.transport.aio.buffer.BufferPage;
import io.mio.transport.aio.buffer.VirtualBuffer;
import io.mio.transport.aio.handler.ReadCompletionHandler;
import io.mio.transport.aio.handler.WriteCompletionHandler;
import lombok.Data;
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
 * AioMioSession为mio-aio最核心的类，封装{@link AsynchronousSocketChannel} API接口，简化IO操作。
 *
 * @author lry
 */
@Slf4j
@Data
public class AioMioSession<T> {

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
     * @see AioMioSession#SESSION_STATUS_CLOSED
     * @see AioMioSession#SESSION_STATUS_CLOSING
     * @see AioMioSession#SESSION_STATUS_ENABLED
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
    private VirtualBuffer readVirtualBuffer;
    private VirtualBuffer writeVirtualBuffer;

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
    private InetSocketAddress localAddress;
    private InetSocketAddress remoteAddress;

    /**
     * 输出流
     */
    private WriteBuffer writeBuffer;
    /**
     * 是否处于数据输出中
     */
    private boolean writing = false;
    /**
     * 最近一次读取到的字节数
     */
    private int lastReadSize;

    private Protocol<T> protocol;
    private MessageProcessor<T> messageProcessor;

    /**
     * 数据输出Function
     */
    private Function<WriteBuffer, Void> flushFunction = new Function<WriteBuffer, Void>() {
        @Override
        public Void apply(WriteBuffer var) {
            if (!semaphore.tryAcquire()) {
                return null;
            }
            AioMioSession.this.writeVirtualBuffer = var.poll();
            if (writeVirtualBuffer == null) {
                semaphore.release();
            } else {
                writing = true;
                continueWrite(writeVirtualBuffer);
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
            writeVirtualBuffer = buffer;
            continueWrite(writeVirtualBuffer);
        }
    };

    public AioMioSession(AsynchronousSocketChannel channel,
                         int readBufferSize,
                         int writeQueueCapacity,
                         int bufferPoolChunkSize,
                         Protocol<T> protocol,
                         MessageProcessor<T> messageProcessor,
                         ReadCompletionHandler<T> readCompletionHandler,
                         WriteCompletionHandler<T> writeCompletionHandler,
                         BufferPage bufferPage) {
        this.channel = channel;
        this.protocol = protocol;
        this.messageProcessor = messageProcessor;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;

        try {
            this.localAddress = (InetSocketAddress) channel.getLocalAddress();
            this.remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
        } catch (IOException e) {
            log.error("Parse network socket address exception", e);
        }

        this.readVirtualBuffer = bufferPage.allocate(readBufferSize);
        writeBuffer = new WriteBuffer(bufferPage, flushFunction, writeQueueCapacity, bufferPoolChunkSize, fasterWrite);
        // 触发状态机
        messageProcessor.stateEvent(this, EventState.NEW_SESSION, null);

        // 初始化AioMioSession
        try {
            continueRead();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            close(true);
        }
    }

    /**
     * 触发AIO的写操作,
     * <p>需要调用控制同步</p>
     */
    public void writeToChannel() {
        if (writeVirtualBuffer == null) {
            writeVirtualBuffer = writeBuffer.poll();
        } else if (!writeVirtualBuffer.buffer().hasRemaining()) {
            writeVirtualBuffer.clean();
            writeVirtualBuffer = writeBuffer.poll();
        }

        if (writeVirtualBuffer != null) {
            continueWrite(writeVirtualBuffer);
            return;
        }
        writing = false;
        semaphore.release();
        // 此时可能是Closing或Closed状态
        if (status != SESSION_STATUS_ENABLED) {
            close(true);
        } else {
            // 也许此时有新的消息通过write方法添加到writeCacheQueue中
            writeBuffer.flush();
        }
    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public synchronized void close(boolean immediate) {
        // status == SESSION_STATUS_CLOSED说明close方法被重复调用
        if (status == SESSION_STATUS_CLOSED) {
            log.warn("Session is closed");
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        boolean noWriteBuffer = (writeVirtualBuffer == null || !writeVirtualBuffer.buffer().hasRemaining());
        if (immediate) {
            writeBuffer.close();
            writeBuffer = null;
            readVirtualBuffer.clean();
            readVirtualBuffer = null;
            if (writeVirtualBuffer != null) {
                writeVirtualBuffer.clean();
                writeVirtualBuffer = null;
            }
            close(channel);
            messageProcessor.stateEvent(this, EventState.SESSION_CLOSED, null);
        } else if (noWriteBuffer && !writeBuffer.hasData()) {
            close(true);
        } else {
            messageProcessor.stateEvent(this, EventState.SESSION_CLOSING, null);
            writeBuffer.flush();
        }
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
        final ByteBuffer readBuffer = this.readVirtualBuffer.buffer();
        readBuffer.flip();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            T dataEntry = null;
            try {
                dataEntry = protocol.decode(readBuffer, this);
            } catch (Exception e) {
                messageProcessor.stateEvent(this, EventState.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            // 处理消息
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

        if (!writing && writeBuffer != null) {
            writeBuffer.flush();
        }

        // 数据读取完毕
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {
            // 仅当发生数据读取时调用compact,减少内存拷贝
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }

        // 读缓冲区已满
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
    private void continueRead() {
        if (messageProcessor != null) {
            messageProcessor.beforeRead(this);
        }

        // 触发通道的读操作
        channel.read(readVirtualBuffer.buffer(), this, readCompletionHandler);
    }

    /**
     * 触发写操作
     *
     * @param writeBuffer 存放待输出数据的buffer
     */
    private void continueWrite(VirtualBuffer writeBuffer) {
        if (messageProcessor != null) {
            messageProcessor.beforeWrite(this);
        }

        // 触发通道的写操作
        channel.write(writeBuffer.buffer(), 0L, TimeUnit.MILLISECONDS, this, writeCompletionHandler);
    }

    /**
     * The close
     *
     * @param channel 需要被关闭的通道
     */
    public static void close(AsynchronousSocketChannel channel) {
        if (channel == null) {
            throw new NullPointerException();
        }
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            log.debug("shutdown input exception", e);
        }
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            log.debug("shutdown output exception", e);
        }
        try {
            channel.close();
        } catch (IOException e) {
            log.debug("close channel exception", e);
        }
    }

}
