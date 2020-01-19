package io.mio.transport.netty4.mio;

import io.mio.core.extension.Extension;
import io.mio.core.transport.ClientConfig;
import io.mio.transport.netty4.NettyInitializer;
import io.mio.core.transport.ServerConfig;
import io.netty.channel.ChannelPipeline;

/**
 * NettyMioInitializer
 *
 * @author lry
 */
@Extension("mio")
public class NettyMioInitializer implements NettyInitializer<ChannelPipeline> {

    @Override
    public void server(ServerConfig serverConfig, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioDecoder(serverConfig.getMaxContentLength()));
        pipeline.addLast(new NettyMioEncoder(serverConfig.getMaxContentLength()));
    }

    @Override
    public void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        pipeline.addLast(new NettyMioEncoder(clientConfig.getMaxContentLength()));
        pipeline.addLast(new NettyMioDecoder(clientConfig.getMaxContentLength()));
    }

}
