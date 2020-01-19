package io.mio.core.transport;

import lombok.Data;

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
     */
    private int bossThread = 0;
    /**
     * The work thread(IO thread) number
     */
    private int workerThread = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
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
     * The server socket keepalive status
     */
    private boolean keepalive = true;
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

}