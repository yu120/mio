package cn.ms.mio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIO服务端
 * 
 * @author lry
 */
public class Server implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Server.class);

	private static int DEFAULT_PORT = 12345;
	public volatile static long clientCount = 0;

	public CountDownLatch latch;
	public AsynchronousServerSocketChannel channel;

	public Server() {
		this(DEFAULT_PORT);
	}

	public Server(int port) {
		synchronized (this) {
			try {
				channel = AsynchronousServerSocketChannel.open();// 创建服务端通道
				channel.bind(new InetSocketAddress(port));// 绑定端口
				logger.debug("服务器已启动，端口号：{}", port);
			} catch (IOException e) {
				logger.error("The init is fail.", e);
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new Server(), "Server").start();
	}

	@Override
	public void run() {
		try {
			latch = new CountDownLatch(1);
			channel.accept(this, new MioServerHandler());// 用于接收客户端的连接

			latch.await();
		} catch (Exception e) {
			logger.error("The start is fail.", e);
		}
	}

}