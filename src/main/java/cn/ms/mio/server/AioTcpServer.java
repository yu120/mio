package cn.ms.mio.server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.ms.micro.common.URL;

public class AioTcpServer implements Runnable {

	private AsynchronousChannelGroup asynchronousChannelGroup;
	private AsynchronousServerSocketChannel listener;
	
	private AioAcceptHandler aioAcceptHandler;

	public static void main(String... args) throws Exception {
		AioTcpServer server = new AioTcpServer();
		server.start(URL.valueOf("mio://0.0.0.0:9008/test"));

		new Thread(server).start();
	}

	public void start(URL url) throws Exception {
		int port = url.getPort();
		int coreThreadNum = url.getParameter("coreThreadNum", 20);

		ExecutorService executor = Executors.newFixedThreadPool(coreThreadNum);

		this.asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
		this.listener = AsynchronousServerSocketChannel.open(asynchronousChannelGroup).bind(new InetSocketAddress(port));
		this.aioAcceptHandler = new AioAcceptHandler();
	}

	@Override
	public void run() {
		try {
			/**
			 * 为服务端socket指定接收操作对象.accept原型是:accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler)
			 * 也就是这里的CompletionHandler的A型参数是实际调用accept方法的第一个参数,即是listener。另一个参数V，就是原型中的客户端socket  
			 */
			listener.accept(listener, aioAcceptHandler);
			System.out.println("服务端启动成功!");
			while (true) {
				Thread.sleep(100000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("finished server");
		}
	}

}