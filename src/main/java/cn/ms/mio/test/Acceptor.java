package cn.ms.mio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Acceptor implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	
	private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);
	final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	 
	@Override
	public void completed(AsynchronousSocketChannel socketChannel, AsynchronousServerSocketChannel serverSocketChannel) {
		//注意接收一个连接之后，紧接着可以接收下一个连接，所以必须再次调用accept方法
        serverSocketChannel.accept(null, this);
        try {
        	logger.info("Incoming connection from : " + socketChannel.getRemoteAddress());
            while (socketChannel.read(buffer).get() != -1) {
                buffer.flip();
                final ByteBuffer duplicate = buffer.duplicate();
                final CharBuffer decode = Charset.defaultCharset().decode(duplicate);
                System.out.println("服务端收到请求："+decode.toString());
                socketChannel.write(buffer).get();
                if (buffer.hasRemaining()) {
                    buffer.compact();
                } else {
                    buffer.clear();
                }
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
        	logger.error("",e);
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
            	logger.error("",e);
            }
        }
	}

	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel serverSocketChannel) {
		serverSocketChannel.accept(null, this);
        throw new UnsupportedOperationException("Cannot accept connections!");
	}

}
