package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
import io.mio.core.extension.ExtensionLoader;
import io.mio.core.serialize.Serialize;
import io.mio.core.transport.ClientConfig;
import io.mio.transport.netty4.NettyInitializer;
import io.mio.core.transport.ServerConfig;
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
    public void server(ServerConfig serverConfig, ChannelPipeline pipeline) {
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(serverConfig.getSerialize());

        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(serverConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpDecoder(serialize));
        pipeline.addLast(new NettyHttpServerEncoder(serialize));
    }

    @Override
    public void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        Serialize serialize = ExtensionLoader.getLoader(Serialize.class).getExtension(clientConfig.getSerialize());

        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpObjectAggregator(clientConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpClientEncoder(serialize));
        pipeline.addLast(new NettyHttpDecoder(serialize));
    }

}
