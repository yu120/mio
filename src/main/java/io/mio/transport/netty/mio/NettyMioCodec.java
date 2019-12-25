package io.mio.transport.netty.mio;

import io.mio.transport.Codec;
import io.mio.serialize.Serialize;
import io.mio.commons.extension.Extension;
import io.netty.channel.ChannelPipeline;

/**
 * NettyMioCodec
 *
 * @author lry
 */
@Extension("mio")
public class NettyMioCodec implements Codec<ChannelPipeline> {

    @Override
    public void server(int maxContentLength, Serialize serialize, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioDecoder(maxContentLength, serialize));
        pipeline.addLast(new NettyMioEncoder(maxContentLength, serialize));
    }

    @Override
    public void client(int maxContentLength, Serialize serialize, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioEncoder(maxContentLength, serialize));
        pipeline.addLast(new NettyMioDecoder(maxContentLength, serialize));
    }

}