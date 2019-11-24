package io.mio.client;

import io.mio.protocol.MioDecoder;
import io.mio.protocol.MioEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private ChannelFuture channelFuture;

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.connect("0.0.0.0", 9999);
        while (true) {
            Thread.sleep(100000);
        }
    }

    public void connect(String host, int port) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new MioEncoder());
                            ch.pipeline().addLast(new MioDecoder());
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            channelFuture = bootstrap.connect(host, port).sync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void send() {
        channelFuture.channel();
    }
    public void destroy() {
        if (eventLoopGroup != null) {
            try {
                channelFuture.channel().closeFuture().sync();
                eventLoopGroup.shutdownGracefully();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
