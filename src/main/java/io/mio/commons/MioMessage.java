package io.mio.commons;

import lombok.Data;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MioMessage
 * <p>
 * Tip: MIO Unified message
 *
 * @author lry
 */
@Data
public class MioMessage implements Serializable {

    /**
     * The header parameters
     */
    private Map<String, Object> headers;
    /**
     * The header parameters data
     */
    private byte[] header;
    /**
     * Body data
     */
    private byte[] data;

    /**
     * The local net socket address
     */
    private InetSocketAddress localAddress;
    /**
     * The remote net socket address
     */
    private InetSocketAddress remoteAddress;

    /**
     * The build new {@link MioMessage}
     * <p>
     * Tips: need set contentLength,metaLength,meta.
     *
     * @param headers header parameters
     * @param data    data byte[]
     */
    public MioMessage(Map<String, Object> headers, byte[] header, byte[] data) {
        if (headers == null || headers.isEmpty()) {
            this.headers = new LinkedHashMap<>();
        } else {
            this.headers = new LinkedHashMap<>(headers);
        }
        this.header = header;
        this.data = data;
    }

    /**
     * The wrapper {@link SocketAddress}
     *
     * @param localAddress  {@link SocketAddress}
     * @param remoteAddress {@link SocketAddress}
     */
    public void wrapper(SocketAddress localAddress, SocketAddress remoteAddress) {
        this.localAddress = (InetSocketAddress) localAddress;
        this.remoteAddress = (InetSocketAddress) remoteAddress;
    }

}
