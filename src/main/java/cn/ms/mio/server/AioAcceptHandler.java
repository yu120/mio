package cn.ms.mio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public class AioAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

	/**
	 * 接收请求成功
	 * 
	 * @param result The Client Socket
	 * @param attachment The Server Socket
	 */
	@Override
	public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
		try {
			System.out.println("AioAcceptHandler.completed called");
			attachment.accept(attachment, this);
			System.out.println("有客户端连接:" + result.getRemoteAddress().toString());
			
			this.doReadProcessor(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
		exc.printStackTrace();
	}

	private void doReadProcessor(AsynchronousSocketChannel clientSocket) {
		ByteBuffer clientBuffer = ByteBuffer.allocate(1024);//分配1MB的空间作为缓冲空间
		clientSocket.read(clientBuffer, 30000l, TimeUnit.MILLISECONDS, clientBuffer, new AioReadHandler(clientSocket));
	}

}