package cn.ms.mio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.micro.common.URL;

public class MioServer {

	private static final Logger logger = LoggerFactory.getLogger(MioServer.class);

	private AsynchronousChannelGroup threadGroup = null;
	private AsynchronousServerSocketChannel serverSocketChannel = null;

	private Processor processor;
	private MioServerHandler mioServerHandler;

	public void start(URL url, Processor processor) {
		this.processor = processor;
		
		try {
			int coreThread = url.getParameter("coreThread", 10);
			threadGroup = AsynchronousChannelGroup.withFixedThreadPool(coreThread, Executors.defaultThreadFactory());
		} catch (IOException e) {
			logger.error("The create thread group is exception.", e);
		}

		try {
			serverSocketChannel = AsynchronousServerSocketChannel.open(threadGroup);
			if (serverSocketChannel.isOpen()) {
				serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);// 表示在前一个连接处于TIME_WAIT状态时，下一个连接是否可以重用通道地址
				serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 8 * 1024);// 设置通道接收的字节大小

				serverSocketChannel.bind(new InetSocketAddress(url.getHost(), url.getPort()));
				logger.info("Waiting for connections...");

				mioServerHandler = new MioServerHandler(this.processor);
				serverSocketChannel.accept(serverSocketChannel, mioServerHandler);
				threadGroup.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} else {
				logger.warn("The connection cannot be opened!");
			}
		} catch (Exception e) {
			logger.error("The started mio server is exception.", e);
		}
	}

	public void shutdown() {
		try {
			if (serverSocketChannel != null) {
				serverSocketChannel.close();
			}
			if (threadGroup != null) {
				threadGroup.shutdown();
			}
		} catch (IOException e) {
			logger.error("The shutdown mio server is exception.", e);
		}
	}

}
