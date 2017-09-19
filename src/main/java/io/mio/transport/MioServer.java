package io.mio.transport;

import io.mio.transport.handler.ReadCompletionHandler;
import io.mio.transport.handler.WriteCompletionHandler;
import io.mio.transport.processor.MessageFilter;
import io.mio.transport.processor.MessageProcessor;
import io.mio.transport.protocol.Protocol;
import io.mio.transport.support.MioConf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIO服务端
 * 
 * @author lry
 */
public class MioServer<T> {
	
    private static final Logger logger = LoggerFactory.getLogger(MioServer.class);
    
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private MioConf<T> config = new MioConf<T>(true);
    private ReadCompletionHandler<T> readCompletionHandler = new ReadCompletionHandler<T>();
    private WriteCompletionHandler<T> writeCompletionHandler = new WriteCompletionHandler<T>();

    public void start() throws IOException {
        final AtomicInteger threadIndex = new AtomicInteger(0);
        asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(config.getThreadNum(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "AIO-Thread-" + threadIndex.incrementAndGet());
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
                	logger.error("", e);
                }
                MioSession<T> session = new MioSession<T>(channel, config, readCompletionHandler, writeCompletionHandler);
                config.getProcessor().initSession(session);
                session.readFromChannel();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            	logger.warn("", exc);
            }
        });
    }

    public void shutdown() {
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
        	logger.error("", e);
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
	public MioServer<T> setFilters(MessageFilter<T>...filters) {
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

    /**
     * 设置输出队列缓冲区长度
     *
     * @param size
     * @return
     */
    public MioServer<T> setWriteQueueSize(int size) {
        this.config.setWriteQueueSize(size);
        return this;
    }
    
}
