package cn.ms.mio.test;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:负责服务端的写操作
 */
public class Writer implements WriterCallback {
	private static final Logger logger = LoggerFactory.getLogger(Writer.class);

    private ByteBuffer byteBuffer;
    public Writer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        logger.info("A writer has been created!");
    }
    @Override
    public void completed(Integer result, AsynchronousSocketChannel socketChannel) {
    	logger.debug("Message write successfully, size = " + result);
    	logger.info(String.format("Writer name : %s ", Thread.currentThread().getName()));
        byteBuffer.clear();
        socketChannel.read(byteBuffer, socketChannel, new Reader(byteBuffer));
    }
    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel socketChannel) {
    	logger.error("",exc);
        throw new RuntimeException(exc);
    }
}