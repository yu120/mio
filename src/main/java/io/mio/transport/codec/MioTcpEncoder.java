package io.mio.transport.codec;

import io.mio.transport.protocol.MioTcpProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Mio TCP Encoder
 *
 * @author lry
 */
public class MioTcpEncoder extends MessageToByteEncoder<MioTcpProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MioTcpProtocol msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getHeadData());
        out.writeInt(msg.getHeaderLength());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getHeader());
        out.writeBytes(msg.getContent());
    }

}
