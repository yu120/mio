package io.mio.aio2;

import io.mio.ClientConfig;
import io.mio.MioCallback;
import io.mio.MioConstants;
import io.mio.MioMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

/**
 * AioMioClient
 *
 * @author lry
 */
@Slf4j
@Getter
public class AioMioClient {

    private CountDownLatch countDownLatch;
    private ThreadPoolExecutor threadPoolExecutor;
    private AsynchronousSocketChannel socketChannel;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private AioMioClientHandler aioMioClientHandler;

    public void initialize(ClientConfig clientConfig) {
        ThreadFactory workerThreadFactory = MioConstants.newThreadFactory("mio-worker", true);

        this.countDownLatch = new CountDownLatch(1);
        this.aioMioClientHandler = new AioMioClientHandler();
        this.threadPoolExecutor = new ThreadPoolExecutor(clientConfig.getClientThread(), clientConfig.getClientThread(),
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), workerThreadFactory);

        try {
            //创建使用的公共线程池资源
            this.asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(threadPoolExecutor);
            //获取AsynchronousSocketChannel的实例,这里可以跟服务器端一样传入一个AsynchronousChannelGroup的对象
            this.socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
            //连接服务器
            socketChannel.connect(MioConstants.buildSocketAddress(clientConfig.getHostname(),
                    clientConfig.getPort()), this, aioMioClientHandler);

            countDownLatch.await();
        } catch (Exception e) {
            log.error("启动异常", e);
        }
    }

    public MioMessage callback(final MioMessage mioMessage, final MioCallback<MioMessage> callback) throws Throwable {
        aioMioClientHandler.callback(mioMessage, callback);
        return mioMessage;
    }

    public void destroy() {
        try {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
            }
            if (socketChannel != null) {
                socketChannel.close();
            }
            if (asynchronousChannelGroup != null) {
                asynchronousChannelGroup.shutdown();
            }
        } catch (Exception e) {
            log.error("关闭异常", e);
        }
    }

}

