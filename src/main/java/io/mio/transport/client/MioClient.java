package io.mio.transport.client;

import io.mio.transport.codec.ICodec;
import io.mio.transport.codec.MioTcpCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MioClient {
    /**
     * 连接服务器
     *
     * @param port
     * @param host
     * @throws Exception
     */
    public void connect(int port, String host) throws Exception {
        ICodec<ChannelHandler> codec = new MioTcpCodec();

        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 客户端辅助启动类 对客户端配置
            Bootstrap b = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 添加自定义协议的编解码工具
                            ch.pipeline().addLast(codec.encoder());
                            ch.pipeline().addLast(codec.decoder());
                            // 处理网络IO
                            ch.pipeline().addLast(new MioClientHandler());
                        }
                    });
            // 异步链接服务器 同步等待链接成功
            ChannelFuture f = b.connect(host, port).sync();
            // 等待链接关闭
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
            System.out.println("客户端优雅的释放了线程资源...");
        }

    }

    public static void main(String[] args) throws Exception {
        new MioClient().connect(9001, "127.0.0.1");
    }


}
