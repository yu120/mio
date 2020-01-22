package io.mio.transport.netty4.mio;

import io.mio.core.extension.Extension;
import io.mio.core.extension.ExtensionLoader;
import io.mio.core.serialize.Serialize;
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
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(serverConfig.getSerialize());

        pipeline.addLast(new NettyMioDecoder(serverConfig.getMaxContentLength(), serialize));
        pipeline.addLast(new NettyMioEncoder(serverConfig.getMaxContentLength(), serialize));
    }

    @Override
    public void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(clientConfig.getSerialize());

        pipeline.addLast(new NettyMioEncoder(clientConfig.getMaxContentLength(), serialize));
        pipeline.addLast(new NettyMioDecoder(clientConfig.getMaxContentLength(), serialize));
    }

}
