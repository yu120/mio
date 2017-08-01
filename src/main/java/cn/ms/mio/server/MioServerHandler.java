package cn.ms.mio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 作为handler接收客户端连接
 * 
 * @author lry
 */
public class MioServerHandler implements CompletionHandler<AsynchronousSocketChannel, MioServer> {

	@Override
	public void completed(AsynchronousSocketChannel channel, MioServer serverHandler) {
		MioServer.clientCount++;// 继续接受其他客户端的请求
		System.out.println("连接的客户端数：" + MioServer.clientCount);

		serverHandler.channel.accept(serverHandler, this);
		ByteBuffer buffer = ByteBuffer.allocate(1024);// 创建新的Buffer
		channel.read(buffer, buffer, new ReadHandler(channel));// 异步读,第三个参数为接收消息回调的业务Handler
	}

	@Override
	public void failed(Throwable exc, MioServer serverHandler) {
		exc.printStackTrace();
		serverHandler.latch.countDown();
	}

}