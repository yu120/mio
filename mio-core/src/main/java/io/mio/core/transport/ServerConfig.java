package io.mio.core.transport;

import lombok.Data;

import java.io.InputStream;
import java.io.Serializable;

/**
 * ServerConfig
 *
 * @author lry
 */
@Data
public class ServerConfig implements Serializable {

    /**
     * The boss thread number
     * <p>
     * 0 = current_processors_amount * 2
     */
    private int bossThread = 0;
    /**
     * The work thread(IO thread) number
     * <p>
     * 0 = current_processors_amount * 2
     */
    private int workerThread = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
    /**
     * The use linux native epoll
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
     * The server transport
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
     * The server socket backlog size
     */
    private int backlog = 1024;
    /**
     * The server socket tcpKeepalive status
     */
    private boolean tcpKeepalive = true;
    /**
     * The support max channel number
     */
    private int maxConnections = 20000;
    /**
     * Socket max content byte length(byte, default：10MB)
     */
    private int maxContentLength = 10 * 1024 * 1024;
    /**
     * The heartbeat time(ms)
     */
    private int heartbeat = 60 * 1000;

    /**
     * The timeout millis(ms) to shutdown
     */
    private int shutdownTimeoutMillis = 10 * 1000;

    public boolean sslEnabled;
    private String sslProtocol = "TLSv1.2";

    private String keyStoreFormat = "PKCS12";
    private String keyStore;
    private String keyStorePassword = "123456";

    private String trustStoreFormat = "PKCS12";
    private String trustStore;
    private String trustStorePassword = "123456";

}
