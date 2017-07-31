package cn.ms.mio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * AIO服务端
 * 
 * @author lry
 */
public class Server implements Runnable {

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
				System.out.println("服务器已启动，端口号：" + port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new Server(), "Server").start();
	}

	@Override
	public void run() {
		/**
		 * CountDownLatch初始化<br>
		 * 的作用：在完成一组正在执行的操作之前，允许当前的现场一直阻塞<br>
		 * 此处，让现场在此阻塞，防止服务端执行完成后退出<br>
		 * 也可以使用while(true)+sleep<br>
		 * 生成环境就不需要担心这个问题，以为服务端是不会退出的
		 **/
		latch = new CountDownLatch(1);
		channel.accept(this, new AcceptHandler());// 用于接收客户端的连接
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}