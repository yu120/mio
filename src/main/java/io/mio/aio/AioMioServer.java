package io.mio.aio;

import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.handler.ReadCompletionHandler;
import io.mio.aio.handler.WriteCompletionHandler;
import io.mio.aio.support.AioMioSession;
import io.mio.aio.support.EventState;
import io.mio.aio.support.IoServerConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * AIO服务端
 *
 * @param <T> 消息对象类型
 * @author lry
 */
@Slf4j
@Getter
public class AioMioServer<T> implements Runnable {

    /**
     * Server端服务配置。
     * <p>调用AioQuickServer的各setXX()方法，都是为了设置config的各配置项</p>
     */
    private IoServerConfig<T> config = new IoServerConfig<>();
    /**
     * 内存池
     */
    private BufferPagePool bufferPool;

    private ReadCompletionHandler<T> aioReadCompletionHandler;
    private WriteCompletionHandler<T> aioWriteCompletionHandler;
    /**
     * 连接会话实例化Function
     */
    private Function<AsynchronousSocketChannel, AioMioSession<T>> aioSessionFunction;

    private AsynchronousServerSocketChannel serverSocketChannel = null;
    private AsynchronousChannelGroup asynchronousChannelGroup;

    /**
     * accept线程运行状态
     */
    private volatile boolean acceptRunning = true;


    public AioMioServer(int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
        config.setThreadNum(Runtime.getRuntime().availableProcessors());
    }

    public AioMioServer(String host, int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        this(port, protocol, messageProcessor);
        config.setHost(host);
    }

    /**
     * 启动Server端的AIO服务
     *
     * @throws IOException IO异常
     */
    public void start() throws IOException {
        checkAndResetConfig();
        this.aioSessionFunction = channel -> new AioMioSession<T>(channel, config,
                aioReadCompletionHandler, aioWriteCompletionHandler, bufferPool.allocateBufferPage());
        this.aioReadCompletionHandler = new ReadCompletionHandler<>(new Semaphore(config.getThreadNum() - 1));
        this.aioWriteCompletionHandler = new WriteCompletionHandler<>();
        this.bufferPool = new BufferPagePool(config.getBufferPoolPageSize(), config.getBufferPoolPageNum(),
                config.getBufferPoolSharedPageSize(), config.isBufferPoolDirect());

        try {
            this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    config.getThreadNum(), r -> bufferPool.newThread(r, "mio-aio:Worker-"));
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            //set socket options
            if (config.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            //bind host
            if (config.getHost() != null) {
                serverSocketChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()), 1000);
            } else {
                serverSocketChannel.bind(new InetSocketAddress(config.getPort()), 1000);
            }

            Thread acceptThread = new Thread(this);
            acceptThread.setDaemon(true);
            acceptThread.setPriority(1);
            acceptThread.start();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
        log.info("mio-aio server[{}] started.", config);
    }

    @Override
    public void run() {
        Future<AsynchronousSocketChannel> nextFuture = serverSocketChannel.accept();
        while (acceptRunning) {
            try {
                final AsynchronousSocketChannel channel = nextFuture.get();
                nextFuture = serverSocketChannel.accept();
                if (config.getMonitor() == null || config.getMonitor().shouldAccept(channel)) {
                    createSession(channel);
                } else {
                    config.getProcessor().stateEvent(null, EventState.REJECT_ACCEPT, null);
                    log.warn("reject accept channel:{}", channel);
                    AioMioSession.close(channel);
                }
            } catch (Exception e) {
                log.error("accept exception", e);
            }
        }
    }

    /**
     * 检查配置项
     */
    private void checkAndResetConfig() {
        //确保单核CPU默认初始化至少2个线程
        if (config.getThreadNum() == 1) {
            config.setThreadNum(2);
        }
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
     *
     * @param channel 当前已建立连接通道
     */
    private void createSession(AsynchronousSocketChannel channel) {
        //连接成功则构造AIOSession对象
        AioMioSession<T> session = null;
        try {
            session = aioSessionFunction.apply(channel);
            session.initSession();
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            if (session == null) {
                AioMioSession.close(channel);
            } else {
                session.close(true);
            }
        }
    }

    public void shutdown() {
        acceptRunning = false;
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
        aioReadCompletionHandler.shutdown();
    }

}
