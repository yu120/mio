package io.mio.transport.netty4;

import io.mio.core.MioConstants;
import io.mio.core.transport.MioServer;
import io.mio.core.commons.*;
import io.mio.core.extension.Extension;
import io.mio.core.extension.ExtensionLoader;
import io.mio.core.extension.TypeReference;
import io.mio.core.transport.ServerConfig;
import io.mio.transport.netty4.http.SslContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * NettyMioServer
 *
 * @author lry
 */
@Slf4j
@Extension("netty")
public class NettyMioServer implements MioServer {

    private ServerConfig serverConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel serverChannel;
    private NettyMioServerHandler serverHandler;
    private NettyInitializer<ChannelPipeline> nettyInitializer;

    @Override
    public void initialize(final ServerConfig serverConfig, final MioCallback<MioMessage> mioCallback) {
        this.serverConfig = serverConfig;

        ThreadFactory bossThreadFactory = MioConstants.newThreadFactory("mio-server-boss", true);
        ThreadFactory workerThreadFactory = MioConstants.newThreadFactory("mio-server-worker", true);

        // create handler
        this.serverHandler = new NettyMioServerHandler(serverConfig, mioCallback);

        // create socket channel type and handler
        Class<? extends ServerChannel> channelClass;
        if (serverConfig.isUseLinuxNativeEpoll()) {
            // select socket channel type
            channelClass = EpollServerSocketChannel.class;
            this.bossGroup = new EpollEventLoopGroup(serverConfig.getBossThread(), bossThreadFactory);
            this.workerGroup = new EpollEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        } else {
            channelClass = NioServerSocketChannel.class;
            this.bossGroup = new NioEventLoopGroup(serverConfig.getBossThread(), bossThreadFactory);
            this.workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        }

        // create nettyInitializer
        this.nettyInitializer = ExtensionLoader.getLoader(new TypeReference<NettyInitializer<ChannelPipeline>>() {
        }).getExtension(serverConfig.getCodec());

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(channelClass)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // Set TCP buffer size
                    .option(ChannelOption.SO_BACKLOG, serverConfig.getBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isTcpKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    // Using object pools, reusing buffers
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            // SSL
                            SslContextFactory.server(serverConfig, ch.pipeline());
                            // server nettyInitializer
                            nettyInitializer.server(serverConfig, ch.pipeline());
                            // heartbeat detection
                            if (serverConfig.getHeartbeat() > 0) {
                                ch.pipeline().addLast(new IdleStateHandler(0, 0,
                                        serverConfig.getHeartbeat(), TimeUnit.MILLISECONDS));
                            }
                            // process network IO
                            ch.pipeline().addLast(serverHandler);
                        }
                    });

            SocketAddress socketAddress = MioConstants.buildSocketAddress(serverConfig.getHostname(), serverConfig.getPort());
            ChannelFuture channelFuture = serverBootstrap.bind(socketAddress);
            channelFuture.syncUninterruptibly();
            serverChannel = channelFuture.channel();

            log.info("The server started success:{}", serverConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(NettyMioServer.this::destroy));
        } catch (Exception e) {
            log.error("The server initialize exception", e);
        }
    }

    @Override
    public void send(MioMessage mioMessage) throws Throwable {
        String key = String.format("%s->%s", MioConstants.getSocketAddressKey(mioMessage.getLocalAddress()),
                MioConstants.getSocketAddressKey(mioMessage.getRemoteAddress()));
        Channel clientChannel = serverHandler.getChannels().get(key);
        if (clientChannel == null) {
            throw new MioException(MioException.NOT_FOUND_CLIENT, "Not found client:" + key);
        }

        // send
        clientChannel.writeAndFlush(mioMessage);
    }

    @Override
    public void destroy() {
        // close server channel
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (Exception e) {
                log.error("Close server channel exception", e);
            }
        }

        // close handler channel
        if (serverHandler != null) {
            ConcurrentMap<String, Channel> channels = serverHandler.getChannels();
            for (Map.Entry<String, Channel> entry : channels.entrySet()) {
                try {
                    Channel channel = entry.getValue();
                    if (channel != null) {
                        channel.close();
                    }
                } catch (Exception e) {
                    log.error("Close client channel exception: {}", entry.getKey(), e);
                }
            }
        }

        // shutdown worker thread pool
        if (workerGroup != null) {
            try {
                workerGroup.shutdownGracefully(0,
                        serverConfig.getShutdownTimeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("Shutdown server workerGroup exception", e);
            }
        }

        // shutdown boss thread pool
        if (bossGroup != null) {
            try {
                bossGroup.shutdownGracefully(0,
                        serverConfig.getShutdownTimeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("Shutdown server bossGroup exception", e);
            }
        }
    }

}
