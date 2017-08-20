package cn.ms.mio.test;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Description:负责服务端的读消息
 */
public class Reader implements ReaderCallback {
	private static final Logger logger = LoggerFactory.getLogger(Reader.class);

    private ByteBuffer byteBuffer;
    public Reader(ByteBuffer byteBuffer) {
    	logger.info("An reader has been created!");
        this.byteBuffer = byteBuffer;
    }
    @Override
    public void completed(Integer result, AsynchronousSocketChannel socketChannel) {
    	logger.info(String.format("Reader name : %s ", Thread.currentThread().getName()));
        byteBuffer.flip();
        logger.info("Message size : " + result);
        if (result != null && result < 0) {
            try {
                socketChannel.close();
            } catch (IOException e) {
            	logger.error("",e);
            }
            return;
        }
        try {
            SocketAddress localAddress = socketChannel.getLocalAddress();
            SocketAddress remoteAddress = socketChannel.getRemoteAddress();
            logger.info("localAddress : " + localAddress.toString());
            logger.info("remoteAddress : " + remoteAddress.toString());
            socketChannel.write(byteBuffer, socketChannel, new Writer(byteBuffer));
        } catch (IOException e) {
        	logger.error("",e);
        }
        ByteBuffer duplicate = byteBuffer.duplicate();
        CharBuffer decode = Charset.defaultCharset().decode(duplicate);
        logger.info("Receive message from client : " + decode.toString());
    }
    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
    	logger.error("",exc);
        throw new RuntimeException(exc);
    }
}