package io.mio.netty;

import io.mio.Codec;
import io.mio.commons.extension.Extension;
import io.mio.Serialize;
import io.mio.netty.codec.NettyMioDecoder;
import io.mio.netty.codec.NettyMioEncoder;
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
