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
public class NettyHttpInitializer implements NettyInitializer {

    @Override
    public void server(int maxContentLength, Serialize serialize, Compress compress, Object attachment) {
        ChannelPipeline ch = (ChannelPipeline) attachment;
        ch.addLast(new HttpRequestDecoder());
        ch.addLast(new HttpResponseEncoder());
        ch.addLast(new HttpObjectAggregator(maxContentLength));
        ch.addLast(new NettyHttpDecoder(serialize));
        ch.addLast(new NettyHttpEncoder(true, serialize));
    }

    @Override
    public void client(int maxContentLength, Serialize serialize, Compress compress, Object attachment) {
        ChannelPipeline ch = (ChannelPipeline) attachment;
        ch.addLast(new HttpRequestEncoder());
        ch.addLast(new HttpResponseDecoder());
        ch.addLast(new HttpObjectAggregator(maxContentLength));
        ch.addLast(new NettyHttpEncoder(false, serialize));
        ch.addLast(new NettyHttpDecoder(serialize));
    }

}
