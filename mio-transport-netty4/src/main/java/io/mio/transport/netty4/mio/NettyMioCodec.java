package io.mio.transport.netty4.mio;

import io.mio.core.extension.Extension;
import io.mio.core.transport.Codec;
import io.netty.channel.ChannelPipeline;

/**
 * NettyMioCodec
 *
 * @author lry
 */
@Extension("mio")
public class NettyMioCodec implements Codec<ChannelPipeline> {

    @Override
    public void server(int maxContentLength, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioDecoder(maxContentLength));
        pipeline.addLast(new NettyMioEncoder(maxContentLength));
    }

    @Override
    public void client(int maxContentLength, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioEncoder(maxContentLength));
        pipeline.addLast(new NettyMioDecoder(maxContentLength));
    }

}
