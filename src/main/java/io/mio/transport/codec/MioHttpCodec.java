package io.mio.transport.codec;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Codec
 *
 * @author lry
 */
public class MioHttpCodec implements ICodec<ChannelHandler> {

    @Override
    public List<ChannelHandler> decodeEncode() {
        List<ChannelHandler> channelHandlerList = new ArrayList<>();
        channelHandlerList.add(new HttpRequestDecoder());
        channelHandlerList.add(new HttpResponseEncoder());

        // Convert multiple requests from HTTP to FullHttpRequest/FullHttpResponse
        channelHandlerList.add(new HttpObjectAggregator(Integer.MAX_VALUE));
        return channelHandlerList;
    }

    @Override
    public List<ChannelHandler> encodeDecode() {
        List<ChannelHandler> channelHandlerList = new ArrayList<>();
        channelHandlerList.add(new HttpRequestEncoder());
        channelHandlerList.add(new HttpResponseDecoder());

        // Convert multiple requests from HTTP to FullHttpRequest/FullHttpResponse
        channelHandlerList.add(new HttpObjectAggregator(Integer.MAX_VALUE));
        return channelHandlerList;
    }

}
