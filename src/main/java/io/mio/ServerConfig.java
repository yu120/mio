package io.mio;

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
     * The server process hold(blocking waiting to be closed)
     */
    private boolean hold = true;
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

}
