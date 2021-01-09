package io.mio.core.transport.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.mio.core.MioConstants;
import io.mio.core.MioException;
import io.mio.core.MioMessage;
import io.mio.core.MioProcessor;
import io.mio.core.compress.Compress;
import io.mio.core.extension.Extension;
import io.mio.core.extension.ExtensionLoader;
import io.mio.core.serialize.Serialize;
import io.mio.core.transport.MioServer;
import io.mio.core.transport.ServerConfig;
import io.mio.core.transport.netty.http.SslContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
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
    private NettyInitializer initializer;

    private static boolean IS_LINUX_PLATFORM = false;

    static {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("linux")) {
            if (osName.toLowerCase().contains("linux")) {
                IS_LINUX_PLATFORM = true;
            }
        }
    }

    @Override
    public void initialize(final ServerConfig serverConfig, final MioProcessor<MioMessage> mioProcessor) {
        this.serverConfig = serverConfig;
        this.serverHandler = new NettyMioServerHandler(serverConfig, mioProcessor);
        this.initializer = ExtensionLoader.getLoader(NettyInitializer.class).getExtension(serverConfig.getCodec());

        // create socket channel type and thread group
        Class<? extends ServerChannel> channelClass;
        ThreadFactory bossThreadFactory = new ThreadFactoryBuilder().setNameFormat("mio-server-boss").setDaemon(true).build();
        ThreadFactory workerThreadFactory = new ThreadFactoryBuilder().setNameFormat("mio-server-worker").setDaemon(true).build();
        if (IS_LINUX_PLATFORM && serverConfig.isUseLinuxNativeEpoll() && Epoll.isAvailable()) {
            channelClass = EpollServerSocketChannel.class;
            this.bossGroup = new EpollEventLoopGroup(serverConfig.getBossThread(), bossThreadFactory);
            this.workerGroup = new EpollEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        } else {
            channelClass = NioServerSocketChannel.class;
            this.bossGroup = new NioEventLoopGroup(serverConfig.getBossThread(), bossThreadFactory);
            this.workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        }

        // create serialize and compress
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(serverConfig.getSerialize());
        Compress compress = ExtensionLoader.getLoader(Compress.class).getExtension(serverConfig.getCompress());

        try {
            // create server bootstrap
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
                            // server initializer
                            initializer.server(serverConfig.getMaxContentLength(), serialize, compress, ch.pipeline());
                            // heartbeat detection
                            if (serverConfig.getHeartbeat() > 0) {
                                ch.pipeline().addLast(new IdleStateHandler(0, 0,
                                        serverConfig.getHeartbeat(), TimeUnit.MILLISECONDS));
                            }
                            // process network IO
                            ch.pipeline().addLast(serverHandler);
                        }
                    });

            // bind port
            SocketAddress socketAddress = MioConstants.buildSocketAddress(serverConfig.getHostname(), serverConfig.getPort());
            ChannelFuture channelFuture = serverBootstrap.bind(socketAddress).syncUninterruptibly();
            this.serverChannel = channelFuture.channel();

            // add shutdown hook
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
