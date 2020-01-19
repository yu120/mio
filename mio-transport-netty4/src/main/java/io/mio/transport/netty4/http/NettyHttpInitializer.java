package io.mio.transport.netty4.http;

import io.mio.core.extension.Extension;
import io.mio.core.transport.ClientConfig;
import io.mio.transport.netty4.NettyInitializer;
import io.mio.core.transport.ServerConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;

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
            pipeline.addLast("sslHandler", getSslHandler(serverConfig));
        }

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
    private SslHandler getSslHandler(ServerConfig serverConfig) {
        try {
            SSLContext sslContext = createSslContext(serverConfig);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);
            return new SslHandler(sslEngine);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private SSLContext createSslContext(ServerConfig serverConfig) throws Exception {
        TrustManager[] managers = null;
        if (serverConfig.getTrustStore() != null) {
            KeyStore ts = KeyStore.getInstance(serverConfig.getTrustStoreFormat());
            ts.load(getStoreStream(serverConfig.getTrustStore()), serverConfig.getTrustStorePassword().toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            managers = tmf.getTrustManagers();
        }

        KeyStore ks = KeyStore.getInstance(serverConfig.getKeyStoreFormat());
        ks.load(getStoreStream(serverConfig.getKeyStore()), serverConfig.getKeyStorePassword().toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, serverConfig.getKeyStorePassword().toCharArray());

        SSLContext serverContext = SSLContext.getInstance(serverConfig.getSslProtocol());
        serverContext.init(kmf.getKeyManagers(), managers, null);
        return serverContext;
    }

    private InputStream getStoreStream(String storeFile) {
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(storeFile);
        } catch (FileNotFoundException e1) {
            inputStream = NettyHttpInitializer.class.getResourceAsStream(storeFile);
            if (inputStream == null) {
                try {
                    inputStream = new FileInputStream(System.getProperty("user.home") + File.separator + storeFile);
                } catch (FileNotFoundException e3) {
                    throw new IllegalStateException(e3);
                }
            }
        }

        return inputStream;
    }

}
