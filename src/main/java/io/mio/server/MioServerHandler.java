package io.mio.server;

import io.mio.protocol.MioProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MioServerHandler extends SimpleChannelInboundHandler<MioProtocol> {

    /**
     * 用于获取客户端发送的信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MioProtocol msg) throws Exception {
        // 用于获取客户端发来的数据信息
        System.out.println("Server接受的客户端的信息 :" + msg.toString());

        // 会写数据给客户端
        String str = "Hi I am MioServer ...";
        MioProtocol response = new MioProtocol(str.getBytes().length, str.getBytes());
        // 当服务端完成写操作后，关闭与客户端的连接
        ctx.writeAndFlush(response);
        // .addListener(ChannelFutureListener.CLOSE);

        // 当有写操作时，不需要手动释放msg的引用
        // 当只有读操作时，才需要手动释放msg的引用
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
