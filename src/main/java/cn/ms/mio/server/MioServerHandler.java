package cn.ms.mio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 作为handler接收客户端连接
 * 
 * @author lry
 */
public class MioServerHandler implements CompletionHandler<AsynchronousSocketChannel, Server> {

	@Override
	public void completed(AsynchronousSocketChannel channel, Server serverHandler) {
		Server.clientCount++;// 继续接受其他客户端的请求
		System.out.println("连接的客户端数：" + Server.clientCount);

		serverHandler.channel.accept(serverHandler, this);
		ByteBuffer buffer = ByteBuffer.allocate(1024);// 创建新的Buffer
		channel.read(buffer, buffer, new ReadHandler(channel));// 异步读,第三个参数为接收消息回调的业务Handler
	}

	@Override
	public void failed(Throwable exc, Server serverHandler) {
		exc.printStackTrace();
		serverHandler.latch.countDown();
	}

}