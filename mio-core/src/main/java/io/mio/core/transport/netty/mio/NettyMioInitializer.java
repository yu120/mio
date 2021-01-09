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
public class NettyMioInitializer implements NettyInitializer<ChannelPipeline> {

    @Override
    public void server(int maxContentLength, Serialize serialize, Compress compress, ChannelPipeline attachment) {
        attachment.addLast(new NettyMioDecoder(maxContentLength, serialize, compress));
        attachment.addLast(new NettyMioEncoder(maxContentLength, serialize, compress));
    }

    @Override
    public void client(int maxContentLength, Serialize serialize, Compress compress, ChannelPipeline attachment) {
        attachment.addLast(new NettyMioEncoder(maxContentLength, serialize, compress));
        attachment.addLast(new NettyMioDecoder(maxContentLength, serialize, compress));
    }

}
