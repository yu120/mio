package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
import io.mio.core.transport.Codec;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;

/**
 * NettyHttpCodec
 *
 * @author lry
 */
@Extension("http")
public class NettyHttpCodec implements Codec<ChannelPipeline> {

    @Override
    public void server(int maxContentLength, ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));

        pipeline.addLast(new NettyHttpDecoder());
        pipeline.addLast(new NettyHttpServerEncoder());
    }

    @Override
    public void client(int maxContentLength, ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));

        pipeline.addLast(new NettyHttpClientEncoder());
        pipeline.addLast(new NettyHttpDecoder());
    }

}
