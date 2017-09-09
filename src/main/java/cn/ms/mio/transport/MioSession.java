package cn.ms.mio.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.mio.transport.handler.ReadCompletionHandler;
import cn.ms.mio.transport.handler.WriteCompletionHandler;
import cn.ms.mio.transport.processor.MessageFilter;
import cn.ms.mio.transport.support.MioConf;
import cn.ms.mio.transport.support.SessionStatus;

/**
 * AIO传输层会话
 * 
 * @author lry
 */
public class MioSession<T> {
	
    private static final Logger logger = LoggerFactory.getLogger(MioSession.class);
    /**
     * Session ID生成器
     */
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    /**
     * 唯一标识
     */
    private final int sessionId = NEXT_ID.getAndIncrement();


    /**
     * 会话当前状态
     */
    private volatile byte status = SessionStatus.SESSION_STATUS_ENABLED;

    /**
     * 会话属性,延迟创建以减少内存消耗
     */
    private Map<String, Object> attribute;

    /**
     * 响应消息缓存队列
     */
    private ArrayBlockingQueue<ByteBuffer> writeCacheQueue;

    /**
     * 缓存传输层读取到的数据流
     */
    private ByteBuffer readBuffer;

    private ReadCompletionHandler<T> readCompletionHandler;
    private WriteCompletionHandler<T> writeCompletionHandler;

    /**
     * 数据输出Handler附件
     */
    private SimpleEntry<MioSession<T>, ByteBuffer> writeAttach = new SimpleEntry<MioSession<T>, ByteBuffer>(this, null);


    /**
     * 数据read限流标志,仅服务端需要进行限流
     */
    private AtomicBoolean serverFlowLimit;

    /**
     * 底层通信channel对象
     */
    private AsynchronousSocketChannel channel;


    /**
     * 输出信号量
     */
    private Semaphore semaphore = new Semaphore(1);


    private MioConf<T> mioConf;

    public MioSession(AsynchronousSocketChannel channel, MioConf<T> config, ReadCompletionHandler<T> readCompletionHandler, WriteCompletionHandler<T> writeCompletionHandler) {
        this.readBuffer = ByteBuffer.allocate(config.getReadBufferSize());
        this.channel = channel;
        this.serverFlowLimit = config.isServer() ? new AtomicBoolean(false) : null;
        this.readCompletionHandler = readCompletionHandler;
        this.writeCompletionHandler = writeCompletionHandler;
        this.writeCacheQueue = new ArrayBlockingQueue<ByteBuffer>(config.getWriteQueueSize());
        this.mioConf = config;
    }

    /**
     * 触发AIO的写操作,
     * <p>需要调用控制同步</p>
     */
    public void writeToChannel(ByteBuffer writeBuffer) {
        if (isInvalid()) {
            close();
            logger.warn("end write because of aioSession's status is" + status);
            return;
        }
        ByteBuffer nextBuffer = writeCacheQueue.peek();//为null说明队列已空
        if (writeBuffer == null && nextBuffer == null) {
            semaphore.release();
            if (writeCacheQueue.size() > 0 && semaphore.tryAcquire()) {
                writeToChannel(null);
            }
            return;
        }
        if (writeBuffer == null) {
            //对缓存中的数据进行压缩处理再输出
            Iterator<ByteBuffer> iterable = writeCacheQueue.iterator();
            int totalSize = 0;
            while (iterable.hasNext()) {
                totalSize += iterable.next().remaining();
                if (totalSize >= 32 * 1024) {
                    break;
                }
            }
            writeBuffer = ByteBuffer.allocate(totalSize);
            while (writeBuffer.hasRemaining()) {
                writeBuffer.put(writeCacheQueue.poll());
            }
            writeBuffer.flip();
        } else if (nextBuffer != null && nextBuffer.remaining() <= (writeBuffer.capacity() - writeBuffer.remaining())) {
            writeBuffer.compact();
            do {
                writeBuffer.put(writeCacheQueue.poll());
            }
            while ((nextBuffer = writeCacheQueue.peek()) != null && nextBuffer.remaining() <= writeBuffer.remaining());
            writeBuffer.flip();
        }

        writeAttach.setValue(writeBuffer);
        channel.write(writeBuffer, writeAttach, writeCompletionHandler);
    }

    /**
     * 如果存在流控并符合释放条件，则触发读操作
     */
    public void tryReleaseFlowLimit() {
        if (serverFlowLimit != null && serverFlowLimit.get() && writeCacheQueue.size() < mioConf.getReleaseLine()) {
            serverFlowLimit.set(false);
            channel.read(readBuffer, this, readCompletionHandler);
        }

    }

    public void write(final ByteBuffer buffer) throws IOException {
        if (isInvalid()) {
            return;
        }
        buffer.flip();
        try {
            //正常读取
            writeCacheQueue.put(buffer);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        if (semaphore.tryAcquire()) {
            writeToChannel(null);
        }
    }

    public final void close() {
        close(true);
    }


    /**
     * * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public void close(boolean immediate) {
        if (immediate) {
            try {
                channel.close();
                logger.debug("close connection:" + channel);
            } catch (IOException e) {
                logger.error("", e);
            }
            status = SessionStatus.SESSION_STATUS_CLOSED;
        } else {
            status = SessionStatus.SESSION_STATUS_CLOSING;
            if (writeCacheQueue.isEmpty() && semaphore.tryAcquire()) {
                close(true);
                semaphore.release();
            }
        }
    }


    @SuppressWarnings("unchecked")
    public final <T1> T1 getAttribute(String key) {
        return attribute == null ? null : (T1) attribute.get(key);
    }

    /**
     * 获取当前Session的唯一标识
     *
     * @return
     */
    public final int getSessionID() {
        return sessionId;
    }

    /**
     * 当前会话是否已失效
     */
    public boolean isInvalid() {
        return status != SessionStatus.SESSION_STATUS_ENABLED;
    }

    /**
     * 触发通道的读操作，当发现存在严重消息积压时,会触发流控
     */
    public void readFromChannel() {
        readBuffer.flip();
        // 将从管道流中读取到的字节数据添加至当前会话中以便进行消息解析
        T dataEntry;
        int remain = 0;
        while ((remain = readBuffer.remaining()) > 0 && (dataEntry = mioConf.getProtocol().decode(readBuffer, this)) != null) {
            receive0(this, dataEntry, remain - readBuffer.remaining());
        }
        //数据读取完毕
        if (readBuffer.remaining() == 0) {
            readBuffer.clear();
        } else if (readBuffer.position() > 0) {// 仅当发生数据读取时调用compact,减少内存拷贝
            readBuffer.compact();
        } else {
            readBuffer.position(readBuffer.limit());
            readBuffer.limit(readBuffer.capacity());
        }

        //触发流控
        if (serverFlowLimit != null && writeCacheQueue.size() > mioConf.getFlowLimitLine()) {
            serverFlowLimit.set(true);
        } else {
            channel.read(readBuffer, this, readCompletionHandler);
        }
    }

    public final void removeAttribute(String key) {
        if (attribute != null) {
            attribute.remove(key);
        }
    }


    public final void setAttribute(String key, Object value) {
        if (attribute == null) {
            attribute = new HashMap<String, Object>();
        }
        attribute.put(key, value);
    }


    public final void write(T t) throws IOException {
        write(mioConf.getProtocol().encode(t, this));
    }

    /**
     * 接收并处理消息
     *
     * @param session
     * @param dataEntry
     * @param readSize
     */
    private void receive0(MioSession<T> session, T dataEntry, int readSize) {
        if (mioConf.getFilters() == null) {
            try {
                mioConf.getProcessor().process(session, dataEntry);
            } catch (Exception e) {
                logger.error("", e);
            }
            return;
        }

        // 接收到的消息进行预处理
        for (MessageFilter<T> h : mioConf.getFilters()) {
            h.readFilter(session, dataEntry, readSize);
        }
        try {
            for (MessageFilter<T> h : mioConf.getFilters()) {
                h.processFilter(session, dataEntry);
            }
            mioConf.getProcessor().process(session, dataEntry);
        } catch (Exception e) {
            logger.error("", e);
            for (MessageFilter<T> h : mioConf.getFilters()) {
                h.processFailHandler(session, dataEntry, e);
            }
        }
    }
    
}
