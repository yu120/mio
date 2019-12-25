package io.mio.netty;

import io.mio.Codec;
import io.mio.MioClient;
import io.mio.Serialize;
import io.mio.commons.*;
import io.mio.commons.extension.Extension;
import io.mio.commons.extension.ExtensionLoader;
import io.mio.commons.extension.TypeReference;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * NettyMioClient
 *
 * @author lry
 */
@Slf4j
@Extension("netty")
public class NettyMioClient implements MioClient {

    public static final AttributeKey<MioCallback<MioMessage>> MIO_CALLBACK_KEY = AttributeKey.valueOf("MIO_CALLBACK");

    private ClientConfig clientConfig;
    private EventLoopGroup eventLoopGroup;
    private AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> channelPools;

    private NettyMioClientHandler clientHandler;
    private Serialize serialize;
    private Codec<ChannelPipeline> codec;

    @Override
    public void initialize(ClientConfig clientConfig) {
        ThreadFactory threadFactory = MioConstants.newThreadFactory("mio-client-worker", true);

        // create group and handler
        this.clientConfig = clientConfig;
        this.eventLoopGroup = new NioEventLoopGroup(clientConfig.getClientThread(), threadFactory);
        this.clientHandler = new NettyMioClientHandler();

        // create serialize and codec
        this.serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(clientConfig.getSerialize());
        this.codec = ExtensionLoader.getLoader(new TypeReference<Codec<ChannelPipeline>>() {
        }).getExtension(clientConfig.getCodec());

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
                            // client codec
                            codec.client(clientConfig.getMaxContentLength(), serialize, ch.pipeline());
                            // heartbeat detection
                            if (clientConfig.getHeartbeat() > 0) {
                                ch.pipeline().addLast(new IdleStateHandler(0, 0,
                                        clientConfig.getHeartbeat(), TimeUnit.MILLISECONDS));
                            }
                            // process network IO
                            ch.pipeline().addLast(clientHandler);
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

    @Override
    public MioMessage request(final MioMessage mioMessage) throws Throwable {
        return submit(mioMessage).get();
    }

    @Override
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

    @Override
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

    @Override
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
                eventLoopGroup.shutdownGracefully(0,
                        clientConfig.getShutdownTimeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("Shutdown client eventLoopGroup exception", e);
            }
        }
    }

}
