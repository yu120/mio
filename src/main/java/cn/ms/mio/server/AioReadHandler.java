package cn.ms.mio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	private AsynchronousSocketChannel clientSocket;
	private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

	public AioReadHandler(AsynchronousSocketChannel asynchronousSocketChannel) {
		this.clientSocket = asynchronousSocketChannel;
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		if (result > 0) {
			attachment.flip();
			try {
				System.out.println("Server-Received-Request("+clientSocket.getRemoteAddress().toString()+"):"+ decoder.decode(attachment));
				attachment.compact();
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			clientSocket.read(attachment, attachment, this);
		} else if (result == -1) {
			try {
				System.out.println("Server->Client-Break:" + clientSocket.getRemoteAddress().toString());
				attachment = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer buf) {
		exc.printStackTrace();
	}

}