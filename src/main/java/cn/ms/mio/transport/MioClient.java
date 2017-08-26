package cn.ms.mio.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

import cn.ms.mio.filter.DefaultMioFilterChain;
import cn.ms.mio.filter.MioFilter;
import cn.ms.mio.protocol.Protocol;
import cn.ms.mio.transport.support.IProcessor;
import cn.ms.mio.transport.support.IoServerConfig;
import cn.ms.mio.transport.support.MioReadHandler;
import cn.ms.mio.transport.support.MioSession;
import cn.ms.mio.transport.support.MioWriteHandler;

public class MioClient<T> {
   
	private AsynchronousSocketChannel socketChannel = null;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private IoServerConfig<T> config;

    public MioClient(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.config = new IoServerConfig<T>(false);
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    public MioClient() throws IOException {
        this.config = new IoServerConfig<T>(false);
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        this.socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort())).get();
        final MioSession<T> session = new MioSession<T>(socketChannel, config, new MioReadHandler<T>(), new MioWriteHandler<T>(), new DefaultMioFilterChain<T>(config.getProcessor(), config.getFilters()));
        config.getProcessor().initSession(session);
        session.channelReadProcess(false);
    }

    public void shutdown() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
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
     * 设置协议对象的构建工厂
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
    public MioClient<T> setFilters(MioFilter<T>[] filters) {
        this.config.setFilters(filters);
        return this;
    }

    /**
     * 设置消息处理器
     *
     * @param processor
     * @return
     */
    public MioClient<T> setProcessor(IProcessor<T> processor) {
        this.config.setProcessor(processor);
        return this;
    }
    
    public IoServerConfig<T> getConfig() {
		return this.config;
	}
    
    public MioSession<T> getSession() {
    	return this.getConfig().getProcessor().getSession();
	}

}
