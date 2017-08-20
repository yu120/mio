package cn.ms.mio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Description:基于Future实现的可以接受多客户端并发的Java NIO2服务端实现
 */
public class ServerOnFutureForMultiClients {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerOnFutureForMultiClients.class);
	
    static final int DEFAULT_PORT = 7777;
    static final String IP = "127.0.0.1";
    static ExecutorService taskExecutorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    
    public static void main(String[] args) {
        try (AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open()) {
            if (serverSocketChannel.isOpen()) {
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                serverSocketChannel.bind(new InetSocketAddress(IP, DEFAULT_PORT));
                logger.info("Waiting for connections...");
                while (true) {
                    Future<AsynchronousSocketChannel> socketChannelFuture = serverSocketChannel.accept();
                    try {
                        final AsynchronousSocketChannel socketChannel = socketChannelFuture.get();
                        Callable<String> worker = new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                String s = socketChannel.getRemoteAddress().toString();
                                logger.info("Incoming connection from : " + s);
                                while (socketChannel.read(buffer).get() != -1) {
                                    buffer.flip();
                                    ByteBuffer duplicate = buffer.duplicate();
                                    showMessage(duplicate);
                                    socketChannel.write(buffer).get();
                                    if (buffer.hasRemaining()) {
                                        buffer.compact();
                                    } else {
                                        buffer.clear();
                                    }
                                }
                                socketChannel.close();
                                logger.info(s + " was successfully served!");
                                return s;
                            }
                        };
                        taskExecutorService.submit(worker);
                    } catch (InterruptedException | ExecutionException e) {
                    	logger.error("", e);
                        taskExecutorService.shutdown();
                        while (!taskExecutorService.isTerminated()) {}
                        break;
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
        System.out.println(decode.toString());
    }
}