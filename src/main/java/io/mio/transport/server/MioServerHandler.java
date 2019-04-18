package io.mio.transport.server;

import io.mio.transport.protocol.MioTcpProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MioServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 用于获取客户端发来的数据信息
        MioTcpProtocol body = (MioTcpProtocol) msg;
        System.out.println("Server接受的客户端的信息 :" + body.toString());

        // 会写数据给客户端
        String str = "Hi I am Server ...";
        MioTcpProtocol response = new MioTcpProtocol(str.getBytes().length, str.getBytes());

        // 当服务端完成写操作后，关闭与客户端的连接
        ctx.writeAndFlush(response);
        // .addListener(ChannelFutureListener.CLOSE);

        // 当有写操作时，不需要手动释放msg的引用
        // 当只有读操作时，才需要手动释放msg的引用
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}