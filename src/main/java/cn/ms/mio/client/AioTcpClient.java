package cn.ms.mio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AioTcpClient {

	private AsynchronousChannelGroup asyncChannelGroup;

	public void start() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(20);
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
	}

	public void send(String ip, int port, byte[] data) throws Exception {
		try {
			AsynchronousSocketChannel connector = null;
			if (connector == null || !connector.isOpen()) {
				connector = AsynchronousSocketChannel.open(asyncChannelGroup);
				// 设置TCP参数
				connector.setOption(StandardSocketOptions.TCP_NODELAY, true);
				connector.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
				
				
				InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
				AioConnectHandler aioConnectHandler =new AioConnectHandler(data);
				connector.connect(inetSocketAddress, connector, aioConnectHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) throws Exception {
		AioTcpClient client = new AioTcpClient();
		client.start();
		
		// 启动200个并发连接，使用20个线程的池子
		for (int i = 0; i < 200; i++) {
			client.send("localhost", 9008, ("测试报文"+i).getBytes());
		}
	}
}