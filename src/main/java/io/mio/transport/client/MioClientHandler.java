package io.mio.transport.client;

import io.mio.transport.protocol.MioTcpProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MioClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String headerStr = "这是请求头内容";
        String contentStr = "这是请求体内容";
        MioTcpProtocol protocol = new MioTcpProtocol(headerStr.getBytes(), contentStr.getBytes());

        ctx.writeAndFlush(protocol);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            MioTcpProtocol body = (MioTcpProtocol) msg;
            System.out.println("Client接受的客户端的信息 :" + body.toString());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
