package io.mio.netty;

import io.mio.Codec;
import io.mio.commons.extension.Extension;
import io.mio.Serialize;
import io.mio.netty.codec.NettyHttpDecoder;
import io.mio.netty.codec.NettyHttpEncoder;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;

/**
 * NettyHttpCode
 *
 * @author lry
 */
@Extension("http")
public class NettyHttpCode implements Codec<ChannelPipeline> {

    @Override
    public void server(int maxContentLength, Serialize serialize, ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));

        pipeline.addLast(new NettyHttpDecoder());
        pipeline.addLast(new NettyHttpEncoder(true));
    }

    @Override
    public void client(int maxContentLength, Serialize serialize, ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));

        pipeline.addLast(new NettyHttpEncoder(false));
        pipeline.addLast(new NettyHttpDecoder());
    }

}