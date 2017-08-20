package cn.ms.mio.client;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AioConnectHandler implements CompletionHandler<Void, AsynchronousSocketChannel> {
	
	private byte[] data;

	public AioConnectHandler(byte[] data) {
		this.data = data;
	}

	@Override
	public void completed(Void attachment, AsynchronousSocketChannel connector) {
		try {
			connector.write(ByteBuffer.wrap(data)).get();
			this.doReceiveRead(connector);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException ep) {
			ep.printStackTrace();
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		exc.printStackTrace();
	}

	private void doReceiveRead(AsynchronousSocketChannel socket) {
		ByteBuffer clientBuffer = ByteBuffer.allocate(1024);
		AioReadHandler aioReadHandler = new AioReadHandler(socket);
		socket.read(clientBuffer, 30000, TimeUnit.MILLISECONDS, clientBuffer, aioReadHandler);
	}
	
}