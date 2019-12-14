package io.mio.aio;

import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.handler.ReadCompletionHandler;
import io.mio.aio.handler.WriteCompletionHandler;
import io.mio.aio.support.AioClientConfig;
import io.mio.aio.support.AioMioSession;
import io.mio.commons.MioConstants;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AIO实现的客户端服务
 *
 * @param <T>
 * @author lry
 */
public class AioMioClient<T> {

    @Getter
    private AioClientConfig<T> config;
    @Setter
    private BufferPagePool bufferPool = null;
    private AioMioSession<T> session;

    private Protocol<T> protocol;
    private MessageProcessor<T> messageProcessor;

    /**
     * IO事件处理线程组。
     * <p>
     * 作为客户端，该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需。
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    public AioMioClient(AioClientConfig<T> config, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        this.config = config;
        this.protocol = protocol;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 启动客户端。
     * <p>
     * 在与服务端建立连接期间，该方法处于阻塞状态。直至连接建立成功，或者发生异常。
     * 该start方法支持外部指定AsynchronousChannelGroup，实现多个客户端共享一组线程池资源，有效提升资源利用率。
     *
     * @param asynchronousChannelGroup IO事件处理线程组
     * @return 建立连接后的会话对象
     * @throws Exception IOException,ExecutionException,InterruptedException
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     */
    public AioMioSession<T> start(AsynchronousChannelGroup asynchronousChannelGroup) throws Exception {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        if (bufferPool == null) {
            this.bufferPool = new BufferPagePool(config.getBufferPoolPageSize(), 1, config.isBufferPoolDirect());
        }

        //set socket options
        if (config.getSocketOptions() != null) {
            for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                socketChannel.setOption(entry.getKey(), entry.getValue());
            }
        }

        try {
            Future<Void> future = socketChannel.connect(new InetSocketAddress(config.getHostname(), config.getPort()));
            if (config.getConnectTimeout() > 0) {
                future.get(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
            } else {
                future.get();
            }
        } catch (TimeoutException e) {
            AioMioSession.close(socketChannel);
            shutdownNow();
            throw new IOException(e);
        }

        //连接成功则构造AIOSession对象
        session = new AioMioSession<T>(socketChannel,
                config.getReadBufferSize(),
                config.getWriteQueueCapacity(),
                config.getBufferPoolChunkSize(),
                protocol, messageProcessor,
                new ReadCompletionHandler<T>(),
                new WriteCompletionHandler<T>(),
                bufferPool.allocateBufferPage());
        session.initSession();
        return session;
    }

    /**
     * 启动客户端。
     * <p>
     * 本方法会构建线程数为2的{@code asynchronousChannelGroup}，并通过调用{@link AioMioClient#start(AsynchronousChannelGroup)}启动服务。
     *
     * @return 建立连接后的会话对象
     * @throws Exception IOException,ExecutionException,InterruptedException
     * @see AioMioClient#start(AsynchronousChannelGroup)
     */
    public final AioMioSession<T> start() throws Exception {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2,
                MioConstants.newThreadFactory("aio-mio-client", true));
        return start(asynchronousChannelGroup);
    }

    public final void shutdownGracefully() {
        showdown0(false);
    }

    public final void shutdownNow() {
        showdown0(true);
    }

    private void showdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
        if (bufferPool != null) {
            bufferPool.release();
        }
    }

}
