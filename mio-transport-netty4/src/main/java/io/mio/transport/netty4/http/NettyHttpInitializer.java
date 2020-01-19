package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
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
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(serverConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpDecoder());
        pipeline.addLast(new NettyHttpServerEncoder());
    }

    @Override
    public void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpObjectAggregator(clientConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpClientEncoder());
        pipeline.addLast(new NettyHttpDecoder());
    }

}
