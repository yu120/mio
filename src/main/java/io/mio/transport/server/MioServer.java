package io.mio.transport.server;

import io.mio.commons.URL;
import io.mio.transport.codec.ICodec;
import io.mio.transport.codec.MioTcpCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * MIO Server
 *
 * @author lry
 */
@Slf4j
public class MioServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;
    private ChannelFuture channelFuture;

    public static void main(String[] args) {
        URL url = URL.valueOf("http://127.0.0.1:9001");
        MioServer mioServer = new MioServer();
        mioServer.initialize(url);
    }

    public void initialize(URL url) {
        ICodec<ChannelHandler> codec = new MioTcpCodec();

        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            List<ChannelHandler> channelHandlerList = codec.decodeEncode();
                            for (ChannelHandler channelHandler : channelHandlerList) {
                                ch.pipeline().addLast(channelHandler);
                            }
                            ch.pipeline().addLast(new MioServerHandler());
                        }
                    });
            channelFuture = serverBootstrap.bind(url.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("The start server is fail", e);
        }
    }

    public void destroy() {
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("The destroy server is fail", e);
        }
    }

}
