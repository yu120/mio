package cn.ms.mio.transport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.ms.mio.protocol.Protocol;
import cn.ms.mio.service.filter.SmartFilter;
import cn.ms.mio.service.filter.SmartFilterChain;
import cn.ms.mio.service.filter.impl.SmartFilterChainImpl;
import cn.ms.mio.service.process.MessageProcessor;
import cn.ms.mio.transport.support.IoServerConfig;
import cn.ms.mio.transport.support.MioReadHandler;
import cn.ms.mio.transport.support.MioSession;
import cn.ms.mio.transport.support.MioWriteHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MioServer<T> {
	
    private static final Logger LOGGER = LogManager.getLogger(MioServer.class);
    
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private IoServerConfig<T> config;
    private MioReadHandler<T> mioReadHandler = new MioReadHandler<T>();
    private MioWriteHandler<T> mioWriteHandler = new MioWriteHandler<T>();
    
    /**
     * 消息过滤器
     */
    private SmartFilterChain<T> smartFilterChain;

    public MioServer() {
        this.config = new IoServerConfig<T>(true);
    }

    public void start() throws IOException {
        smartFilterChain = new SmartFilterChainImpl<T>(config.getProcessor(), config.getFilters());
        final AtomicInteger threadIndex = new AtomicInteger(0);
        asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(config.getThreadNum(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Mio-Server-" + threadIndex.incrementAndGet());
            }
        });

        this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup).bind(new InetSocketAddress(config.getPort()), 1000);
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(final AsynchronousSocketChannel channel, Object attachment) {
                serverSocketChannel.accept(attachment, this);
                try {
                    channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                } catch (IOException e) {
                    LOGGER.catching(e);
                }
                
                MioSession<T> session = new MioSession<T>(channel, config, mioReadHandler, mioWriteHandler, smartFilterChain);
                config.getProcessor().initSession(session);
                session.channelReadProcess(false);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                LOGGER.warn(exc);
            }
        });
    }

    public void shutdown() {
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        asynchronousChannelGroup.shutdown();
    }

    /**
     * 设置服务绑定的端口
     *
     * @param port
     * @return
     */
    public MioServer<T> bind(int port) {
        this.config.setPort(port);
        return this;
    }

    /**
     * 设置处理线程数量
     *
     * @param num
     * @return
     */
    public MioServer<T> setThreadNum(int num) {
        this.config.setThreadNum(num);
        return this;
    }

    public MioServer<T> setProtocol(Protocol<T> protocol) {
        this.config.setProtocol(protocol);
        return this;
    }

    /**
     * 设置消息过滤器,执行顺序以数组中的顺序为准
     *
     * @param filters
     * @return
     */
    @SuppressWarnings("unchecked")
	public MioServer<T> setFilters(SmartFilter<T>... filters) {
        this.config.setFilters(filters);
        return this;
    }

    /**
     * 设置消息处理器
     *
     * @param processor
     * @return
     */
    public MioServer<T> setProcessor(MessageProcessor<T> processor) {
        this.config.setProcessor(processor);
        return this;
    }
    
}
