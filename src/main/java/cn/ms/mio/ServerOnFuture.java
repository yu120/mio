package cn.ms.mio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description:基于Future的NIO2服务端实现，此时的服务端还无法实现多客户端并发，如果有多个客户端并发连接该服务端的话，
 * 客户端会出现阻塞，待前一个客户端处理完毕，服务端才会接受下一个客户端的连接并处理
 * </P>
 */
public class ServerOnFuture {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerOnFuture.class);
	
    static final int DEFAULT_PORT = 7777;
    static final String IP = "127.0.0.1";
    static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    public static void main(String[] args) {
        try (AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open()) {
            if (serverSocketChannel.isOpen()) {
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                serverSocketChannel.bind(new InetSocketAddress(IP, DEFAULT_PORT));
                logger.info("Waiting for connections...");
                while (true) {
                    Future<AsynchronousSocketChannel> channelFuture = serverSocketChannel.accept();
                    try (AsynchronousSocketChannel socketChannel = channelFuture.get()) {
                    	logger.info("Incoming connection from : " + socketChannel.getRemoteAddress());
                        while (socketChannel.read(buffer).get() != -1) {
                            buffer.flip();
                            // Java NIO2或者Java AIO报： java.util.concurrent.ExecutionException: java.io.IOException: 指定的网络名不再可用。
                            // 此处要注意，千万不能直接操作buffer，否则客户端会阻塞并报错，“java.util.concurrent.ExecutionException: java.io.IOException: 指定的网络名不再可用。”
                            ByteBuffer duplicate = buffer.duplicate();
                            showMessage(duplicate);
                            socketChannel.write(buffer).get();
                            if (buffer.hasRemaining()) {
                                buffer.compact();
                            } else {
                                buffer.clear();
                            }
                        }
                        logger.info(socketChannel.getRemoteAddress() + " was successfully served!");
                    } catch (InterruptedException | ExecutionException e) {
                    	logger.error("", e);
                    }
                }
            } else {
            	logger.warn("The asynchronous server-socket channel cannot be opened!");
            }
        } catch (IOException e) {
        	logger.error("", e);
        }
    }
    protected static void showMessage(ByteBuffer buffer) {
        CharBuffer decode = Charset.defaultCharset().decode(buffer);
        logger.info(decode.toString());
        System.out.println(decode.toString());
    }
}