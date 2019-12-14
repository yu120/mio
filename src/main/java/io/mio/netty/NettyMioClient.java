package io.mio.netty;

import io.mio.*;
import io.mio.compress.Compress;
import io.mio.compress.GzipCompress;
import io.mio.netty.protocol.NettyMioDecoder;
import io.mio.netty.protocol.NettyMioEncoder;
import io.mio.serialize.Hessian2Serialize;
import io.mio.serialize.Serialize;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

/**
 * NettyMioClient
 *
 * @author lry
 */
@Slf4j
public class NettyMioClient {

    public static final AttributeKey<MioCallback<MioMessage>> MIO_CALLBACK_KEY = AttributeKey.valueOf("MIO_CALLBACK");

    private ClientConfig clientConfig;
    private EventLoopGroup eventLoopGroup;
    private AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> channelPools;

    private NettyMioClientHandler clientHandler;
    private Serialize serialize;
    private Compress compress;

    /**
     * The initialize client
     *
     * @param clientConfig {@link ClientConfig}
     */
    public void initialize(ClientConfig clientConfig) {
        ThreadFactory threadFactory = MioConstants.newThreadFactory("mio-client-worker", true);

        // create group and handler
        this.clientConfig = clientConfig;
        this.eventLoopGroup = new NioEventLoopGroup(clientConfig.getClientThread(), threadFactory);
        this.clientHandler = new NettyMioClientHandler();
        this.serialize = new Hessian2Serialize();
        this.compress = new GzipCompress();

        try {
            // create client bootstrap
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .channel(NioSocketChannel.class);

            // create fixed channel pool
            this.channelPools = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
                @Override
                protected FixedChannelPool newPool(InetSocketAddress key) {
                    return new FixedChannelPool(bootstrap.remoteAddress(key), new AbstractChannelPoolHandler() {
                        @Override
                        public void channelReleased(Channel ch) throws Exception {
                            // Flush掉所有写回的数据
                            ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
                        }

                        @Override
                        public void channelCreated(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NettyMioEncoder(clientConfig.getMaxContentLength(), serialize, pipeline));
                            pipeline.addLast(new NettyMioDecoder(clientConfig.getMaxContentLength(), serialize, null));
                            if (clientConfig.getHeartbeat() > 0) {
                                pipeline.addLast(new IdleStateHandler(0,
                                        0, clientConfig.getHeartbeat(), TimeUnit.MILLISECONDS));
                            }
                            pipeline.addLast(clientHandler);
                        }
                    }, clientConfig.getMaxConnections());
                }
            };

            // add ShutdownHook
            log.info("The client started success:{}", clientConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(NettyMioClient.this::destroy));
        } catch (Exception e) {
            log.error("Start client exception", e);
        }
    }

    /**
     * The send request
     *
     * @param mioMessage {@link MioMessage}
     * @return {@link MioMessage}
     * @throws Throwable exception {@link Throwable}
     */
    public MioMessage request(final MioMessage mioMessage) throws Throwable {
        return submit(mioMessage).get();
    }

    /**
     * The send submit
     *
     * @param mioMessage {@link MioMessage}
     * @return {@link MioMessageFuture}
     * @throws Throwable exception {@link Throwable}
     */
    public MioMessageFuture<MioMessage> submit(final MioMessage mioMessage) throws Throwable {
        final MioMessageFuture<MioMessage> future = new MioMessageFuture<>();

        // send callback
        this.callback(mioMessage, new MioCallback<MioMessage>() {
            @Override
            public void onSuccess(MioMessage result) {
                future.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable t) {
                future.onFailure(t);
            }
        });

        return future;
    }

    /**
     * The send callback
     *
     * @param mioMessage  {@link MioMessage}
     * @param mioCallback {@link MioCallback}
     * @throws Throwable exception {@link Throwable}
     */
    public void callback(final MioMessage mioMessage, final MioCallback<MioMessage> mioCallback) throws Throwable {
        log.debug("The callback request: {}", mioMessage);
        final FixedChannelPool channelPool = channelPools.get(mioMessage.getRemoteAddress());
        final Channel channel = channelPool.acquire().get();
        if (channel == null) {
            throw new MioException(MioException.CHANNEL_NULL, "Acquire get channel null");
        }
        mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());

        try {
            channel.attr(MIO_CALLBACK_KEY).set(mioCallback.setListener(t -> channelPool.release(channel)));

            // write and flush
            channel.writeAndFlush(mioMessage);
        } catch (Exception e) {
            // return channel to pool
            channelPool.release(channel);
            throw e;
        }
    }

    /**
     * The destroy client
     */
    public void destroy() {
        // close channel pool
        if (channelPools != null) {
            try {
                channelPools.close();
            } catch (Exception e) {
                log.error("Close client channel pool exception", e);
            }
        }

        // shutdown eventLoopGroup
        if (eventLoopGroup != null) {
            try {
                eventLoopGroup.shutdownGracefully();
            } catch (Exception e) {
                log.error("Shutdown client eventLoopGroup exception", e);
            }
        }
    }

}
