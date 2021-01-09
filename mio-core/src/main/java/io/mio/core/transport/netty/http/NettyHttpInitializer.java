package io.mio.core.transport.netty.http;

import io.mio.core.compress.Compress;
import io.mio.core.extension.Extension;
import io.mio.core.serialize.Serialize;
import io.mio.core.transport.netty.NettyInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;

/**
 * NettyHttpInitializer
 *
 * @author lry
 */
@Extension("http")
public class NettyHttpInitializer implements NettyInitializer<ChannelPipeline> {

    @Override
    public void initialize(boolean server, int maxContentLength, Serialize serialize,
                           Compress compress, ChannelPipeline attachment) {
        if (server) {
            attachment.addLast(new HttpRequestDecoder());
            attachment.addLast(new HttpResponseEncoder());
            attachment.addLast(new HttpObjectAggregator(maxContentLength));

            attachment.addLast(new NettyHttpDecoder(serialize));
            attachment.addLast(new NettyHttpServerEncoder(serialize));
        } else {
            attachment.addLast(new HttpRequestEncoder());
            attachment.addLast(new HttpResponseDecoder());
            attachment.addLast(new HttpObjectAggregator(maxContentLength));

            attachment.addLast(new NettyHttpClientEncoder(serialize));
            attachment.addLast(new NettyHttpDecoder(serialize));
        }
    }

}
