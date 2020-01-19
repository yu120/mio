package io.mio.transport.aio;

import io.mio.transport.aio.buffer.BufferPagePool;
import io.mio.transport.aio.handler.ReadCompletionHandler;
import io.mio.transport.aio.handler.WriteCompletionHandler;
import io.mio.transport.aio.support.AioMioSession;
import io.mio.transport.aio.support.AioServerConfig;
import io.mio.transport.aio.support.EventState;
import io.mio.core.MioConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AioMioServer
 *
 * @param <T>
 * @author lry
 */
@Slf4j
public class AioMioServer<T> implements Runnable {

    private AioServerConfig config;
    private BufferPagePool bufferPool;

    private ReadCompletionHandler<T> readCompletionHandler;
    private WriteCompletionHandler<T> writeCompletionHandler;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private volatile boolean acceptRunning = true;
    private ThreadPoolExecutor acceptThreadPoolExecutor;

    private Protocol<T> protocol;
    private MessageProcessor<T> messageProcessor;

    public void initialize(AioServerConfig config, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        this.config = config;
        this.protocol = protocol;
        this.messageProcessor = messageProcessor;
        checkAndResetConfig();
        this.readCompletionHandler = new ReadCompletionHandler<>(new Semaphore(config.getThreadNum() - 1));
        this.writeCompletionHandler = new WriteCompletionHandler<>();
        this.bufferPool = new BufferPagePool(config.getBufferPoolPageSize(), config.getBufferPoolPageNum(),
                config.getBufferPoolSharedPageSize(), config.isBufferPoolDirect());

        try {
            // create channel
            this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    config.getThreadNum(), r -> bufferPool.newThread(r, "aio-mio-worker-"));
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);

            // set socket options
            if (config.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }

            // bind host
            serverSocketChannel.bind(MioConstants.buildSocketAddress(config.getHostname(), config.getPort()), 1000);

            // accept thread pool
            this.acceptThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), MioConstants.newThreadFactory("aio-mio-accept", true));
            acceptThreadPoolExecutor.submit(this);
            log.info("mio-aio server[{}] started.", config);
        } catch (Exception e) {
            log.error("Initialize exception", e);
            destroy();
        }
    }

    @Override
    public void run() {
        Future<AsynchronousSocketChannel> nextFuture = serverSocketChannel.accept();
        while (acceptRunning) {
            try {
                final AsynchronousSocketChannel channel = nextFuture.get();
                nextFuture = serverSocketChannel.accept();
                if (messageProcessor == null || messageProcessor.shouldAccept(channel)) {
                    createSession(channel);
                } else {
                    messageProcessor.stateEvent(null, EventState.REJECT_ACCEPT, null);
                    log.warn("reject accept channel:{}", channel);
                    AioMioSession.close(channel);
                }
            } catch (Exception e) {
                log.error("accept exception", e);
            }
        }
    }

    public void destroy() {
        acceptRunning = false;
        try {
            if (acceptThreadPoolExecutor != null) {
                acceptThreadPoolExecutor.shutdown();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        if (!asynchronousChannelGroup.isTerminated()) {
            try {
                asynchronousChannelGroup.shutdownNow();
            } catch (IOException e) {
                log.error("shutdown exception", e);
            }
        }
        try {
            asynchronousChannelGroup.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("shutdown exception", e);
        }
        if (bufferPool != null) {
            bufferPool.release();
            bufferPool = null;
        }
        readCompletionHandler.shutdown();
    }

    private void checkAndResetConfig() {
        //未指定内存页数量默认等同于线程数
        if (config.getBufferPoolPageNum() <= 0) {
            config.setBufferPoolPageNum(config.getThreadNum());
        }
        //内存页数量不可多于线程数，会造成内存浪费
        if (config.getBufferPoolPageNum() > config.getThreadNum()) {
            throw new RuntimeException("bufferPoolPageNum=" + config.getBufferPoolPageNum() + " can't greater than threadNum=" + config.getThreadNum());
        }
        //内存块不可大于内存页
        if (config.getBufferPoolChunkSize() > config.getBufferPoolPageSize()) {
            throw new RuntimeException("bufferPoolChunkSize=" + config.getBufferPoolChunkSize() + " can't greater than bufferPoolPageSize=" + config.getBufferPoolPageSize());
        }
        //read缓冲区不可大于内存页
        if (config.getReadBufferSize() > config.getBufferPoolPageSize()) {
            throw new RuntimeException("readBufferSize=" + config.getReadBufferSize() + " can't greater than bufferPoolPageSize=" + config.getBufferPoolPageSize());
        }
    }

    /**
     * 为每个新建立的连接创建AIOSession对象
     */
    private void createSession(AsynchronousSocketChannel channel) {
        // 连接成功,则构造AioMioSession对象
        try {
            new AioMioSession<>(channel,
                    config.getReadBufferSize(),
                    config.getWriteQueueCapacity(),
                    config.getBufferPoolChunkSize(),
                    protocol,
                    messageProcessor,
                    readCompletionHandler,
                    writeCompletionHandler,
                    bufferPool.allocateBufferPage());
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            AioMioSession.close(channel);
        }
    }

}
