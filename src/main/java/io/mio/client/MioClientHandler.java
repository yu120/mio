package io.mio.client;

import io.mio.protocol.MioProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * 用于读取客户端发来的信息
 *
 * @author lry
 */
public class MioClientHandler extends SimpleChannelInboundHandler<MioProtocol> {

    /**
     * 客户端与服务端，连接成功的售后
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 发送SmartCar协议的消息

        // 获得要发送信息的字节数组
        byte[] content = ("自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话" +
                "自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话自动化打电话").getBytes();
        MioProtocol protocol = new MioProtocol(content.length, content);
        ctx.writeAndFlush(protocol);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MioProtocol msg) throws Exception {
        try {
            System.out.println("Client接受的客户端的信息 :" + msg.toString());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
