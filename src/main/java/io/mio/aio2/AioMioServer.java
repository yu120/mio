package io.mio.aio2;

import io.mio.commons.MioCallback;
import io.mio.commons.MioConstants;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

import java.net.StandardSocketOptions;
import java.util.concurrent.*;

/**
 * AioMioServer
 *
 * @author lry
 */
@Slf4j
@Getter
public class AioMioServer {

    private CountDownLatch countDownLatch;
    private AioMioServerHandler aioMioServerHandler;
    private ThreadPoolExecutor threadPoolExecutor;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private AsynchronousServerSocketChannel serverSocketChannel;
    private MioCallback<MioMessage> mioCallback;

    /**
     * The initialize
     *
     * @param serverConfig {@link ServerConfig}
     * @param mioCallback     {@link MioCallback}
     */
    public void initialize(ServerConfig serverConfig, MioCallback<MioMessage> mioCallback) {
        ThreadFactory bossThreadFactory = MioConstants.newThreadFactory("mio-worker", true);

        this.mioCallback = mioCallback;
        this.countDownLatch = new CountDownLatch(1);
        this.aioMioServerHandler = new AioMioServerHandler();
        this.threadPoolExecutor = new ThreadPoolExecutor(serverConfig.getWorkerThread(), serverConfig.getWorkerThread(),
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), bossThreadFactory);

        try {
            this.asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(threadPoolExecutor);
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);

            // 接收缓冲区的大小
            serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
            // 是否重用本地地址
            serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

            // 绑定监听的端口号
            SocketAddress socketAddress = MioConstants.buildSocketAddress(serverConfig.getHostname(), serverConfig.getPort());
            serverSocketChannel.bind(socketAddress, serverConfig.getBacklog());

            // 开始接收请求(第1个参数是根据自己的需求出入对应的对象)
            serverSocketChannel.accept(this, aioMioServerHandler);

            log.info("The server started success:{}", serverConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(AioMioServer.this::destroy));
            if (serverConfig.isHold()) {
                countDownLatch.await();
            }
        } catch (Exception e) {
            log.error("The initialize exception", e);
        }
    }

    /**
     * The destroy
     */
    public void destroy() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (asynchronousChannelGroup != null) {
                asynchronousChannelGroup.shutdown();
            }
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
            }
        } catch (Exception e) {
            log.error("The destroy exception", e);
        }
    }

}

