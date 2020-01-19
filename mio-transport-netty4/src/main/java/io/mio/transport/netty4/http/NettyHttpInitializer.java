package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
import io.mio.core.transport.ClientConfig;
import io.mio.transport.netty4.NettyInitializer;
import io.mio.core.transport.ServerConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;
import java.security.KeyStore;

/**
 * NettyHttpInitializer
 *
 * @author lry
 */
@Extension("http")
public class NettyHttpInitializer implements NettyInitializer<ChannelPipeline> {

    private SSLContext sslContext;

    @Override
    public void server(ServerConfig serverConfig, ChannelPipeline pipeline) {
        addSslHandler(serverConfig, pipeline);

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

    /**
     * Adds the ssl handler
     */
    private void addSslHandler(ServerConfig serverConfig, ChannelPipeline pipeline) {
        boolean isSsl = serverConfig.getKeyStore() != null;
        if (isSsl) {
            try {
                sslContext = createSslContext(serverConfig);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        if (sslContext != null) {
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
    }

    private SSLContext createSslContext(ServerConfig serverConfig) throws Exception {
        TrustManager[] managers = null;
        if (serverConfig.getTrustStore() != null) {
            KeyStore ts = KeyStore.getInstance(serverConfig.getTrustStoreFormat());
            ts.load(serverConfig.getTrustStore(), serverConfig.getTrustStorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            managers = tmf.getTrustManagers();
        }

        KeyStore ks = KeyStore.getInstance(serverConfig.getKeyStoreFormat());
        ks.load(serverConfig.getKeyStore(), serverConfig.getKeyStorePassword().toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, serverConfig.getKeyStorePassword().toCharArray());

        SSLContext serverContext = SSLContext.getInstance(serverConfig.getSslProtocol());
        serverContext.init(kmf.getKeyManagers(), managers, null);
        return serverContext;
    }

}
