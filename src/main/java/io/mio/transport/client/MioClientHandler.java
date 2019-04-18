package io.mio.transport.client;

import io.mio.transport.codec.MioTcpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class MioClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String headerStr = "这是请求头内容";
        String contentStr = "这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是" +
                "请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请" +
                "求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体" +
                "内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容这是请求体内容";
        MioTcpMessage protocol = new MioTcpMessage(headerStr.getBytes(), contentStr.getBytes());
        ctx.writeAndFlush(protocol);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            MioTcpMessage body = (MioTcpMessage) msg;
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
