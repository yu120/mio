package io.mio;

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
     * The server bind hostname
     */
    private String hostname;
    /**
     * The server bind port
     */
    private int port = 9999;
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

}
