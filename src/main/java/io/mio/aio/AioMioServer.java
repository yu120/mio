package io.mio.aio;

import io.mio.aio.handler.ReadCompletionHandler;
import io.mio.aio.handler.WriteCompletionHandler;
import io.mio.aio.support.IoServerConfig;
import io.mio.aio.support.TcpAioSession;
import lombok.extern.slf4j.Slf4j;
import io.mio.aio.buffer.BufferPagePool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.InvalidParameterException;
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
public class AioMioServer<T> {

    /**
     * Server端服务配置。
     * <p>调用AioQuickServer的各setXX()方法，都是为了设置config的各配置项</p>
     */
    private IoServerConfig<T> config = new IoServerConfig<>();
    /**
     * 内存池
     */
    private BufferPagePool bufferPool;
    /**
     * 读回调事件处理
     */
    private ReadCompletionHandler<T> aioReadCompletionHandler;
    /**
     * 写回调事件处理
     */
    private WriteCompletionHandler<T> aioWriteCompletionHandler;
    /**
     * 连接会话实例化Function
     */
    private Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction;
    /**
     * asynchronousServerSocketChannel
     */
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    /**
     * asynchronousChannelGroup
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * accept处理线程
     */
    private Thread acceptThread = null;

    /**
     * watcher线程
     */
    private Thread watcherThread;
    /**
     * accept线程运行状态
     */
    private volatile boolean acceptRunning = true;

    /**
     * 设置服务端启动必要参数配置
     *
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioMioServer(int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
        config.setThreadNum(Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param host             绑定服务端Host地址
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
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
        start0(channel -> new TcpAioSession<T>(channel, config, aioReadCompletionHandler, aioWriteCompletionHandler, bufferPool.allocateBufferPage()));
    }

    /**
     * 内部启动逻辑
     *
     * @param aioSessionFunction 实例化会话的Function
     * @throws IOException IO异常
     */
    protected final void start0(Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction) throws IOException {
        checkAndResetConfig();

        try {

            aioReadCompletionHandler = new ReadCompletionHandler<>(new Semaphore(config.getThreadNum() - 1));
            aioWriteCompletionHandler = new WriteCompletionHandler<>();

            this.bufferPool = new BufferPagePool(config.getBufferPoolPageSize(), config.getBufferPoolPageNum(), config.getBufferPoolSharedPageSize(), config.isBufferPoolDirect());
            this.aioSessionFunction = aioSessionFunction;

            asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(config.getThreadNum(), r -> bufferPool.newThread(r, "mio-aio:Worker-"));
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

            startWatcherThread();
            startAcceptThread();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
        log.info("mio-aio server started on port {},threadNum:{}", config.getPort(), config.getThreadNum());
        log.info("mio-aio server config is {}", config);
    }

    private void startAcceptThread() {
        acceptThread = new Thread(new Runnable() {
            private NetFilter<T> monitor = config.getMonitor();

            @Override
            public void run() {
                Future<AsynchronousSocketChannel> nextFuture = serverSocketChannel.accept();
                while (acceptRunning) {
                    try {
                        final AsynchronousSocketChannel channel = nextFuture.get();
                        nextFuture = serverSocketChannel.accept();
                        if (monitor == null || monitor.shouldAccept(channel)) {
                            createSession(channel);
                        } else {
                            config.getProcessor().stateEvent(null, EventState.REJECT_ACCEPT, null);
                            log.warn("reject accept channel:{}", channel);
                            closeChannel(channel);
                        }
                    } catch (Exception e) {
                        log.error("accept exception", e);
                    }
                }
            }
        }, "mio-aio:accept");
        acceptThread.start();
    }

    private void startWatcherThread() {
        watcherThread = new Thread(aioReadCompletionHandler, "mio-aio:watcher");
        watcherThread.setDaemon(true);
        watcherThread.setPriority(1);
        watcherThread.start();
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
        TcpAioSession<T> session = null;
        try {
            session = aioSessionFunction.apply(channel);
            session.initSession();
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            if (session == null) {
                closeChannel(channel);
            } else {
                session.close(true);
            }
        }
    }

    /**
     * 关闭服务端通道
     *
     * @param channel AsynchronousSocketChannel
     */
    private void closeChannel(AsynchronousSocketChannel channel) {
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            log.debug(e.getMessage(), e);
        }
        try {
            channel.close();
        } catch (IOException e) {
            log.debug("close channel exception", e);
        }
    }

    /**
     * 停止服务端
     */
    public final void shutdown() {
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

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置。
     * <p>
     * AIO客户端的有效可选范围为：<br/>
     * 2. StandardSocketOptions.SO_RCVBUF<br/>
     * 4. StandardSocketOptions.SO_REUSEADDR<br/>
     * </p>
     *
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          配置项类型
     * @return 当前AioQuickServer对象
     */
    public final <V> AioMioServer<T> setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 设置write缓冲区容量
     *
     * @param writeQueueCapacity 缓存区容量
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setWriteQueueCapacity(int writeQueueCapacity) {
        config.setWriteQueueCapacity(writeQueueCapacity);
        return this;
    }

    /**
     * 设置服务工作线程数,设置数值必须大于等于2
     *
     * @param threadNum 线程数
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setThreadNum(int threadNum) {
        if (threadNum <= 1) {
            throw new InvalidParameterException("threadNum must >= 2");
        }
        config.setThreadNum(threadNum);
        return this;
    }

    /**
     * 设置单个内存页大小.多个内存页共同组成内存池
     *
     * @param bufferPoolPageSize 内存页大小
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setBufferPoolPageSize(int bufferPoolPageSize) {
        config.setBufferPoolPageSize(bufferPoolPageSize);
        return this;
    }

    /**
     * 设置内存页个数，多个内存页共同组成内存池。
     *
     * @param bufferPoolPageNum 内存页个数
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setBufferPoolPageNum(int bufferPoolPageNum) {
        config.setBufferPoolPageNum(bufferPoolPageNum);
        return this;
    }


    /**
     * 限制写操作时从内存页中申请内存块的大小
     *
     * @param bufferPoolChunkSizeLimit 内存块大小限制
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setBufferPoolChunkSize(int bufferPoolChunkSizeLimit) {
        config.setBufferPoolChunkSize(bufferPoolChunkSizeLimit);
        return this;
    }

    /**
     * 设置内存池是否使用直接缓冲区,默认：true
     *
     * @param isDirect true:直接缓冲区,false:堆内缓冲区
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setBufferPoolDirect(boolean isDirect) {
        config.setBufferPoolDirect(isDirect);
        return this;
    }

    /**
     * 设置共享内存页大小
     *
     * @param bufferPoolSharedPageSize 共享内存页大小
     * @return 当前AioQuickServer对象
     */
    public final AioMioServer<T> setBufferPoolSharedPageSize(int bufferPoolSharedPageSize) {
        config.setBufferPoolSharedPageSize(bufferPoolSharedPageSize);
        return this;
    }
}
