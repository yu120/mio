package io.mio.core.transport.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.mio.core.MioConstants;
import io.mio.core.MioCallback;
import io.mio.core.MioException;
import io.mio.core.MioFuture;
import io.mio.core.MioMessage;
import io.mio.core.compress.Compress;
import io.mio.core.extension.Extension;
import io.mio.core.extension.ExtensionLoader;
import io.mio.core.serialize.Serialize;
import io.mio.core.transport.ClientConfig;
import io.mio.core.transport.MioClient;
import io.mio.core.transport.netty.http.SslContextFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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

    private final AttributeKey<MioCallback<MioMessage>> mioCallbackKey = AttributeKey.valueOf("MIO_CALLBACK");

    private ClientConfig clientConfig;
    private EventLoopGroup eventLoopGroup;
    private AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool> channelPools;

    private NettyMioClientHandler clientHandler;
    private NettyInitializer<ChannelPipeline> initializer;

    @Override
    public void initialize(final ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.clientHandler = new NettyMioClientHandler(mioCallbackKey);
        this.initializer = ExtensionLoader.getLoader(new ExtensionLoader.TypeReference<NettyInitializer<ChannelPipeline>>() {
        }).getExtension(clientConfig.getCodec());

        // create socket channel type and thread group
        Class<? extends SocketChannel> channelClass;
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("mio-client-worker").setDaemon(true).build();
        if (clientConfig.isUseLinuxNativeEpoll()) {
            channelClass = EpollSocketChannel.class;
            this.eventLoopGroup = new EpollEventLoopGroup(clientConfig.getClientThread(), threadFactory);
        } else {
            channelClass = NioSocketChannel.class;
            this.eventLoopGroup = new NioEventLoopGroup(clientConfig.getClientThread(), threadFactory);
        }

        // create serialize and compress
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(clientConfig.getSerialize());
        Compress compress = ExtensionLoader.getLoader(Compress.class).getExtension(clientConfig.getCompress());

        try {
            // create client bootstrap
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .channel(channelClass)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfig.getConnectTimeoutMillis())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

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
                            // SSL
                            SslContextFactory.client(clientConfig, ch.pipeline());
                            // client nettyInitializer
                            initializer.initialize(false, clientConfig.getMaxContentLength(),
                                    serialize, compress, ch.pipeline());
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

            // add shutdown hook
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
    public MioFuture<MioMessage> submit(final MioMessage mioMessage) throws Throwable {
        final MioFuture<MioMessage> mioFuture = new MioFuture<>();
        // send callback
        callback(mioMessage, new MioCallback<MioMessage>() {
            @Override
            public void onSuccess(MioMessage response) {
                mioFuture.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable t) {
                mioFuture.onFailure(t);
            }
        });

        return mioFuture;
    }

    @Override
    public void callback(final MioMessage mioMessage, final MioCallback<MioMessage> mioCallback) throws Throwable {
        log.debug("The callback request: {}", mioMessage);
        final FixedChannelPool channelPool = channelPools.get(mioMessage.getRemoteAddress());
        final Channel channel = channelPool.acquire().get();
        if (channel == null) {
            throw new MioException(MioException.CHANNEL_NULL, "Acquire get channel null");
        }

        try {
            mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());
            channel.attr(mioCallbackKey).set(mioCallback.listener(t -> channelPool.release(channel)));
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
