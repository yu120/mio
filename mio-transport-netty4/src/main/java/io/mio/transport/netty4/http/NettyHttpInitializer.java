package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
import io.mio.core.transport.ClientConfig;
import io.mio.transport.netty4.NettyInitializer;
import io.mio.core.transport.ServerConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;

/**
 * NettyHttpInitializer
 *
 * @author lry
 */
@Extension("http")
public class NettyHttpInitializer implements NettyInitializer<ChannelPipeline> {

    @Override
    public void server(ServerConfig serverConfig, ChannelPipeline pipeline) {
        if (serverConfig.isSslEnabled()) {
            SSLEngine engine = SslContextFactory.getServerContext(serverConfig.getKeyStore(),
                    serverConfig.getTrustStore(), serverConfig.getStorePassword()).createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            pipeline.addLast(new SslHandler(engine));
        }

        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpObjectAggregator(serverConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpDecoder());
        pipeline.addLast(new NettyHttpServerEncoder());
    }

    @Override
    public void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        if (clientConfig.isSslEnabled()) {
            SSLEngine engine = SslContextFactory.getClientContext(clientConfig.getKeyStore(),
                    clientConfig.getTrustStore(), clientConfig.getStorePassword()).createSSLEngine();
            engine.setUseClientMode(true);
            engine.setNeedClientAuth(true);
            pipeline.addLast(new SslHandler(engine));
        }

        pipeline.addLast(new HttpRequestEncoder());
        pipeline.addLast(new HttpResponseDecoder());
        pipeline.addLast(new HttpObjectAggregator(clientConfig.getMaxContentLength()));

        pipeline.addLast(new NettyHttpClientEncoder());
        pipeline.addLast(new NettyHttpDecoder());
    }

}
