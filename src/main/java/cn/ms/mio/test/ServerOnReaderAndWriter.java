package cn.ms.mio.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Description:基于读写类实现的NIO2服务端
 */
public class ServerOnReaderAndWriter {
	private static final Logger logger = LoggerFactory.getLogger(ServerOnReaderAndWriter.class);

    static final int DEFAULT_PORT = 7777;
    static final String IP = "127.0.0.1";
    public static void main(String[] args) {
        try (AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open()) {
            if (serverSocketChannel.isOpen()) {
                //服务端的通道支持两种选项SO_RCVBUF和SO_REUSEADDR，一般无需显式设置，使用其默认即可，此处仅为展示设置方法
                //在面向流的通道中，此选项表示在前一个连接处于TIME_WAIT状态时，下一个连接是否可以重用通道地址
                serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                //设置通道接收的字节大小
                serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 8 * 1024);
                serverSocketChannel.bind(new InetSocketAddress(IP, DEFAULT_PORT));
                logger.info("Waiting for connections...");
                serverSocketChannel.accept(serverSocketChannel, new Acceptor());
                System.in.read();
            } else {
            	logger.warn("The connection cannot be opened!");
            }
        } catch (IOException e) {
        	logger.error("",e);
        }
    }
}