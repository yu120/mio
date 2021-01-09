package io.mio.core.transport.netty.mio;

import io.mio.core.compress.Compress;
import io.mio.core.extension.Extension;
import io.mio.core.serialize.Serialize;
import io.mio.core.transport.netty.NettyInitializer;
import io.netty.channel.ChannelPipeline;

/**
 * NettyMioInitializer
 *
 * @author lry
 */
@Extension("mio")
public class NettyMioInitializer implements NettyInitializer {

    @Override
    public void server(int maxContentLength, Serialize serialize, Compress compress, Object attachment) {
        ChannelPipeline ch = (ChannelPipeline) attachment;
        ch.addLast(new NettyMioDecoder(maxContentLength, serialize, compress));
        ch.addLast(new NettyMioEncoder(maxContentLength, serialize, compress));
    }

    @Override
    public void client(int maxContentLength, Serialize serialize, Compress compress, Object attachment) {
        ChannelPipeline ch = (ChannelPipeline) attachment;
        ch.addLast(new NettyMioEncoder(maxContentLength, serialize, compress));
        ch.addLast(new NettyMioDecoder(maxContentLength, serialize, compress));
    }

}
