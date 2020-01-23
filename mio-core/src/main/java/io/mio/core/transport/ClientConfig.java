package io.mio.core.transport;

import lombok.Data;

import java.io.Serializable;

/**
 * ClientConfig
 *
 * @author lry
 */
@Data
public class ClientConfig implements Serializable {

    /**
     * The client thread(IO thread) number
     */
    private int clientThread = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    /**
     * True is use linux native epoll
     */
    private boolean useLinuxNativeEpoll;
    /**
     * The server bind hostname
     */
    private String hostname;
    /**
     * The server bind port
     */
    private int port = 9999;

    /**
     * The client transport
     */
    private String transport = "netty";
    /**
     * The server codec
     */
    private String codec = "mio";
    /**
     * The server header serialize
     */
    private String serialize = "hessian2";
    /**
     * The compress type
     */
    private String compress = "gzip";
    /**
     * The compress data min length(byte, default：10kb)
     */
    private int compressMinLength = 10 * 1024;

    /**
     * The number of maximal active connections
     */
    private int maxConnections = 20000;
    /**
     * Socket max content byte length(byte)
     */
    private int maxContentLength = 10 * 1024 * 1024;
    /**
     * The heartbeat time(ms)
     */
    private int heartbeat = 60 * 1000;
    /**
     * The timeout millis(ms) to establish connection
     */
    private int connectTimeoutMillis = 10 * 1000;
    /**
     * The timeout millis(ms) to shutdown
     */
    private int shutdownTimeoutMillis = 10 * 1000;

    /**
     * True is enable ssl
     */
    private boolean sslEnabled = true;
    /**
     * The key store file path
     */
    private String keyStore = "nettyClient.jks";
    /**
     * The trust store file path
     */
    private String trustStore = "nettyClient.jks";
    /**
     * The store password
     */
    private String storePassword = "defaultPass";

}
