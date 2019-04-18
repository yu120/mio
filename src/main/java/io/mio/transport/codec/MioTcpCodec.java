package io.mio.transport.codec;

import io.netty.channel.ChannelHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * MioTCP Codec
 *
 * @author lry
 */
public class MioTcpCodec implements ICodec<ChannelHandler> {

    @Override
    public List<ChannelHandler> decodeEncode() {
        List<ChannelHandler> channelHandlerList = new ArrayList<>();
        channelHandlerList.add(new MioTcpDecoder());
        channelHandlerList.add(new MioTcpEncoder());
        return channelHandlerList;
    }

    @Override
    public List<ChannelHandler> encodeDecode() {
        List<ChannelHandler> channelHandlerList = new ArrayList<>();
        channelHandlerList.add(new MioTcpEncoder());
        channelHandlerList.add(new MioTcpDecoder());
        return channelHandlerList;
    }

}
