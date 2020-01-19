package io.mio.transport.aio;

import io.mio.transport.aio.buffer.BufferPagePool;
import io.mio.transport.aio.handler.ReadCompletionHandler;
import io.mio.transport.aio.handler.WriteCompletionHandler;
import io.mio.transport.aio.support.AioClientConfig;
import io.mio.transport.aio.support.AioMioSession;
import io.mio.core.MioConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AioMioClient
 *
 * @param <T>
 * @author lry
 */
public class AioMioClient<T> {

    private BufferPagePool bufferPagePool;
    private AioMioSession<T> session;
    private AsynchronousChannelGroup asynchronousChannelGroup;

    public AioMioSession<T> start(AioClientConfig config, Protocol<T> protocol, BufferPagePool bufferPagePool, MessageProcessor<T> messageProcessor) throws Exception {
        // 作为客户端，该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需。
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2,
                MioConstants.newThreadFactory("aio-mio-client", true));
        return start(config, protocol, bufferPagePool, messageProcessor, asynchronousChannelGroup);
    }

    public AioMioSession<T> start(AioClientConfig config, Protocol<T> protocol, BufferPagePool bufferPagePool, MessageProcessor<T> messageProcessor, AsynchronousChannelGroup asynchronousChannelGroup) throws Exception {
        this.bufferPagePool = bufferPagePool;

        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        if (bufferPagePool == null) {
            this.bufferPagePool = new BufferPagePool(config.getBufferPoolPageSize(), 1, config.isBufferPoolDirect());
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
            destroy();
            throw new IOException(e);
        }

        //连接成功则构造AIOSession对象
        return new AioMioSession<>(socketChannel,
                config.getReadBufferSize(),
                config.getWriteQueueCapacity(),
                config.getBufferPoolChunkSize(),
                protocol, messageProcessor,
                new ReadCompletionHandler<>(),
                new WriteCompletionHandler<>(),
                this.bufferPagePool.allocateBufferPage());
    }

    public void destroy() {
        if (session != null) {
            session.close(false);
            session = null;
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
        if (bufferPagePool != null) {
            bufferPagePool.release();
        }
    }

}
