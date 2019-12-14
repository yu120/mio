package io.mio.netty.codec;

import io.mio.Codec;
import io.mio.extension.Extension;
import io.mio.serialize.Serialize;
import io.netty.channel.ChannelPipeline;

/**
 * NettyMioCode
 *
 * @author lry
 */
@Extension("mio")
public class NettyMioCode implements Codec<ChannelPipeline> {

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
