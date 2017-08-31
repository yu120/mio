package cn.ms.mio.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Random;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.mio.common.MioException;
import cn.ms.mio.common.ParamValue;
import cn.ms.mio.transport.Processor;
import cn.ms.mio.transport.Server;
import cn.ms.neural.NURL;

public class NettyServer implements Server {
	
	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	NURL nurl;
	Processor processor;
	
	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;
	ServerBootstrap serverBootstrap;
	ChannelFuture channelFuture;
	
	NettyHandler nettyHandler;
	
	public NettyServer(NURL nurl, Processor processor) {
		this.nurl = nurl;
		this.processor = processor;
	}
	
	@Override
	public void initialize() throws Exception {
		// The connection number management, the maximum number of connections limit
		int maxServerConnection = nurl.getParameter(ParamValue.maxServerConnection.getName(), ParamValue.maxServerConnection.getIntValue());
		long startupGracefully = nurl.getParameter(ParamValue.startupGracefully.getName(), ParamValue.startupGracefully.getIntValue());
		nettyHandler = new NettyHandler(processor, maxServerConnection, startupGracefully);
		
		// Create IO thread group and server
		int bossThread = nurl.getParameter(ParamValue.bossThread.getName(), ParamValue.bossThread.getIntValue());
		int workerThread = nurl.getParameter(ParamValue.workerThread.getName(), ParamValue.workerThread.getIntValue());
		ThreadFactory bossThreadFactory = new DefaultThreadFactory("NettyServerBossGroup-" + nurl.getHost() + "-" + nurl.getPort(), true);
		ThreadFactory workerThreadFactory = new DefaultThreadFactory("NettyServerWorkerGroup-" + nurl.getHost() + "-" + nurl.getPort(), true);
		bossGroup = new NioEventLoopGroup(bossThread, bossThreadFactory);
		workerGroup = new NioEventLoopGroup(workerThread, workerThreadFactory);
		serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup);
		
		// Set channel type to NIO
		serverBootstrap.channel(NioServerSocketChannel.class);
		
		// Set TCP communication parameters
		serverBootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024);// Send buffer size
		serverBootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024);// Receive buffer size
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);// Set TCP buffer size
		serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);// Keep long connection
		serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
		serverBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000);
		
		// Initialize Handler
		serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			public void initChannel(NioSocketChannel ch) throws Exception {
				//TODO 添加协议层
				
				ch.pipeline().addLast(nettyHandler);// Init service processor
			}
		});
	}

	@Override
	public boolean startup() throws Exception {
		try {
			// The Get Random Port.
	        int port = nurl.getPort();
	        if(port == 0) {
	        	int minPort = nurl.getParameter(ParamValue.minPort.getName(), ParamValue.minPort.getIntValue());
		        int maxPort = nurl.getParameter(ParamValue.maxPort.getName(), ParamValue.maxPort.getIntValue());
		        port = new Random().nextInt(maxPort)%(maxPort-minPort+1) + minPort;
		        if(maxPort < minPort) {
		        	throw new MioException();
		        }
	        }
			
			// The Support Appoint HOST.
			if (ParamValue.host.getValue().equals(nurl.getHost())){
				channelFuture = serverBootstrap.bind(port);
			} else {
				channelFuture = serverBootstrap.bind(nurl.getHost(), port);
			}

			Channel channel = channelFuture.sync().channel();
			if (channel == null || !channel.isActive()) {
				logger.warn("The server[{}:{}] is startted failure!!!", nurl.getHost(), port);
			} else {
				logger.info("The server[{}:{}] is startted success!!!", nurl.getHost(), port);
				boolean hold = nurl.getParameter(ParamValue.hold.getName(), ParamValue.hold.getBooleanValue());
				if (hold) {
					logger.info("The server is startting hold...");
					channel.closeFuture().sync();
				} else {
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("Startup exception", e);
		}
		
		return false;
	}

	@Override
	public void destroy() {
		if (workerGroup != null) {
			long shutdownWorkerTimeout = nurl.getParameter(ParamValue.shutdownWorkerTimeout.getName(), ParamValue.shutdownWorkerTimeout.getLongValue());
			workerGroup.shutdownGracefully(shutdownWorkerTimeout, shutdownWorkerTimeout, TimeUnit.MILLISECONDS);
		}
		if (bossGroup != null) {
			long shutdownBossTimeout = nurl.getParameter(ParamValue.shutdownBossTimeout.getName(), ParamValue.shutdownBossTimeout.getLongValue());
			bossGroup.shutdownGracefully(shutdownBossTimeout, shutdownBossTimeout, TimeUnit.MILLISECONDS);
		}
	}

}
