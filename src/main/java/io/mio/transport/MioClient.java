package io.mio.transport;

import io.mio.transport.handler.ReadCompletionHandler;
import io.mio.transport.handler.WriteCompletionHandler;
import io.mio.transport.processor.MessageFilter;
import io.mio.transport.processor.MessageProcessor;
import io.mio.transport.protocol.Protocol;
import io.mio.transport.support.MioConf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIO实现的客户端服务
 * 
 * @author lry
 */
public class MioClient<T> {
	
    private static final Logger logger = LoggerFactory.getLogger(MioSession.class);
    
    private AsynchronousSocketChannel socketChannel = null;
    /**
     * IO事件处理线程组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * 服务配置
     */
    private MioConf<T> config = new MioConf<T>(false);

    
    public void start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException, ExecutionException, InterruptedException {
        this.socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort())).get();
        final MioSession<T> session = new MioSession<T>(socketChannel, config, new ReadCompletionHandler<T>(), new WriteCompletionHandler<T>());
        config.getProcessor().initSession(session);
        session.readFromChannel();
    }

    /**
     * 启动客户端Socket服务
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void start() throws IOException, ExecutionException, InterruptedException {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        start(asynchronousChannelGroup);
    }

    /**
     * 停止客户端服务
     */
    public void shutdown() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
            	logger.error("The shutdown is exception", e);
            }
        }
        
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
    }

    /**
     * 设置远程连接的地址、端口
     *
     * @param host
     * @param port
     * @return
     */
    public MioClient<T> connect(String host, int port) {
        this.config.setHost(host);
        this.config.setPort(port);
        return this;
    }

    /**
     * 设置协议对象
     *
     * @param protocol
     * @return
     */
    public MioClient<T> setProtocol(Protocol<T> protocol) {
        this.config.setProtocol(protocol);
        return this;
    }

    /**
     * 设置消息过滤器,执行顺序以数组中的顺序为准
     *
     * @param filters
     * @return
     */
    public MioClient<T> setFilters(MessageFilter<T>[] filters) {
        this.config.setFilters(filters);
        return this;
    }

    /**
     * 设置消息处理器
     *
     * @param processor
     * @return
     */
    public MioClient<T> setProcessor(MessageProcessor<T> processor) {
        this.config.setProcessor(processor);
        return this;
    }

}
