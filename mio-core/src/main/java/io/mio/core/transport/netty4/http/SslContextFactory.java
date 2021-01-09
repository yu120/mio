package io.mio.core.transport.netty4.http;

import io.mio.core.transport.ClientConfig;
import io.mio.core.transport.ServerConfig;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

/**
 * SslContextFactory
 *
 * @author lry
 */
public final class SslContextFactory {

    private static final String PROTOCOL = "TLS";
    /**
     * 服务器上下文
     */
    private static SSLContext SERVER_CONTEXT;
    /**
     * 客户端上下文
     */
    private static SSLContext CLIENT_CONTEXT;

    public static void server(ServerConfig serverConfig, ChannelPipeline pipeline) {
        if (serverConfig.isSslEnabled()) {
            SSLEngine engine = SslContextFactory.getServerContext(serverConfig.getKeyStore(),
                    serverConfig.getTrustStore(), serverConfig.getStorePassword()).createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            pipeline.addLast(new SslHandler(engine));
        }
    }

    public static void client(ClientConfig clientConfig, ChannelPipeline pipeline) {
        if (clientConfig.isSslEnabled()) {
            SSLEngine engine = SslContextFactory.getClientContext(clientConfig.getKeyStore(),
                    clientConfig.getTrustStore(), clientConfig.getStorePassword()).createSSLEngine();
            engine.setUseClientMode(true);
            engine.setNeedClientAuth(true);
            pipeline.addLast(new SslHandler(engine));
        }
    }

    private static SSLContext getServerContext(String pkPath, String caPath, String password) {
        if (SERVER_CONTEXT != null) {
            return SERVER_CONTEXT;
        }

        return SERVER_CONTEXT = getContext(pkPath, caPath, password);
    }

    private static SSLContext getClientContext(String pkPath, String caPath, String password) {
        if (CLIENT_CONTEXT != null) {
            return CLIENT_CONTEXT;
        }

        return CLIENT_CONTEXT = getContext(pkPath, caPath, password);
    }

    private static SSLContext getContext(String pkPath, String caPath, String password) {
        if (CLIENT_CONTEXT != null) {
            return CLIENT_CONTEXT;
        }

        InputStream keyInputStream = null;
        InputStream trustInputStream = null;
        try {
            KeyManager[] keyManagers = null;
            if (pkPath != null) {
                KeyStore ks = KeyStore.getInstance("JKS");
                keyInputStream = getStoreStream(pkPath);
                ks.load(keyInputStream, password.toCharArray());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, password.toCharArray());
                keyManagers = kmf.getKeyManagers();
            }

            TrustManager[] trustManagers = null;
            if (caPath != null) {
                KeyStore tks = KeyStore.getInstance("JKS");
                trustInputStream = getStoreStream(caPath);
                tks.load(trustInputStream, password.toCharArray());
                TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
                tf.init(tks);
                trustManagers = tf.getTrustManagers();
            }

            CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);
            CLIENT_CONTEXT.init(keyManagers, trustManagers, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize the client-side SSLContext", e);
        } finally {
            if (keyInputStream != null) {
                try {
                    keyInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (trustInputStream != null) {
                try {
                    trustInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return CLIENT_CONTEXT;
    }

    private static InputStream getStoreStream(String storeFile) {
        InputStream inputStream;

        try {
            inputStream = new FileInputStream(storeFile);
        } catch (FileNotFoundException e1) {
            inputStream = NettyHttpInitializer.class.getResourceAsStream("/" + storeFile);
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
