package cn.ms.mio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Description:基于Future的NIO2客户端实现
 */
public class ClientOnFuture {
    
	private static final Logger logger = LoggerFactory.getLogger(ClientOnFuture.class);
	
	static final int DEFAULT_PORT = 7777;
    static final String IP = "127.0.0.1";
    static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    public static void main(String[] args) {
        try (AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open()) {
            if (socketChannel.isOpen()) {
                //设置一些选项，非必选项，可使用默认设置
                socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
                socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
                Void aVoid = socketChannel.connect(new InetSocketAddress(IP, DEFAULT_PORT)).get();
                //返回null表示连接成功
                if (aVoid == null) {
                    Integer messageLength = socketChannel.write(ByteBuffer.wrap("Hello Server！".getBytes())).get();
                    logger.info(String.valueOf(messageLength));
                    
                    
                    while (socketChannel.read(buffer).get() != -1) {
                        buffer.flip();//写入buffer之后，翻转，之后可以从buffer读取，或者将buffer内容写入通道
                        CharBuffer decode = Charset.defaultCharset().decode(buffer);
                        logger.info(decode.toString());
                        if (buffer.hasRemaining()) {
                            buffer.compact();
                        } else {
                            buffer.clear();
                        }
                        int r = new Random().nextInt(10);
                        if (r == 5) {
                        	logger.info("Client closed!");
                            break;
                        } else {
                            // Java NIO2或者Java AIO报： Exception in thread "main" java.nio.channels.WritePendingException
                            // 此处注意，如果在频繁调用write()的时候，在上一个操作没有写完的情况下，调用write会触发WritePendingException异常，
                            // 所以此处最好在调用write()之后调用get()以便明确等到有返回结果
                            socketChannel.write(ByteBuffer.wrap("Random number : ".concat(String.valueOf(r)).getBytes())).get();
                        }
                    }
                } else {
                	logger.warn("The connection cannot be established!");
                }
            } else {
            	logger.warn("The asynchronous socket-channel cannot be opened!");
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
        	logger.error("", e);
        }
    }
}