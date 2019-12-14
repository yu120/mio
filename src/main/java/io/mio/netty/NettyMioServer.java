package io.mio.netty;

import io.mio.MioServer;
import io.mio.commons.MioCallback;
import io.mio.commons.MioConstants;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;
import io.mio.compress.Compress;
import io.mio.compress.GzipCompress;
import io.mio.extension.Extension;
import io.mio.netty.protocol.NettyMioDecoder;
import io.mio.netty.protocol.NettyMioEncoder;
import io.mio.serialize.Hessian2Serialize;
import io.mio.serialize.Serialize;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel serverChannel;
    private NettyMioServerHandler serverHandler;
    private Serialize serialize;
    private Compress compress;

    @Override
    public void initialize(ServerConfig serverConfig, final MioCallback<MioMessage> mioCallback) {
        ThreadFactory bossThreadFactory = MioConstants.newThreadFactory("mio-server-boss", true);
        ThreadFactory workerThreadFactory = MioConstants.newThreadFactory("mio-server-worker", true);

        // create group and handler
        this.bossGroup = new NioEventLoopGroup(serverConfig.getBossThread(), bossThreadFactory);
        this.workerGroup = new NioEventLoopGroup(serverConfig.getWorkerThread(), workerThreadFactory);
        this.serverHandler = new NettyMioServerHandler(serverConfig.getMaxConnections(), mioCallback);
        this.serialize = new Hessian2Serialize();
        this.compress = new GzipCompress();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // Set TCP buffer size
                    .option(ChannelOption.SO_BACKLOG, serverConfig.getBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, serverConfig.isKeepalive())
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    // Using object pools, reusing buffers
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyMioDecoder(serverConfig.getMaxContentLength(), serialize, ch.pipeline()));
                            ch.pipeline().addLast(new NettyMioEncoder(serverConfig.getMaxContentLength(), serialize, null));
                            // Heartbeat detection
                            if (serverConfig.getHeartbeat() > 0) {
                                ch.pipeline().addLast(new IdleStateHandler(0,
                                        0, serverConfig.getHeartbeat(), TimeUnit.MILLISECONDS));
                            }
                            // Process network IO
                            ch.pipeline().addLast(serverHandler);
                        }
                    });

            SocketAddress socketAddress = MioConstants.buildSocketAddress(serverConfig.getHostname(), serverConfig.getPort());
            ChannelFuture channelFuture = serverBootstrap.bind(socketAddress);
            channelFuture.syncUninterruptibly();
            serverChannel = channelFuture.channel();

            log.info("The server started success:{}", serverConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(NettyMioServer.this::destroy));
            if (serverConfig.isHold()) {
                serverChannel.closeFuture().sync();
            }
        } catch (Exception e) {
            log.error("The server initialize exception", e);
        }
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
                workerGroup.shutdownGracefully();
            } catch (Exception e) {
                log.error("Shutdown server workerGroup exception", e);
            }
        }

        // shutdown boss thread pool
        if (bossGroup != null) {
            try {
                bossGroup.shutdownGracefully();
            } catch (Exception e) {
                log.error("Shutdown server bossGroup exception", e);
            }
        }
    }

}
