package io.mio.core.commons;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@NoArgsConstructor
public class MioMessage implements Serializable {

    /**
     * The data
     */
    private Object data;
    /**
     * The attachments data
     * <p>
     * 1.protocol version
     * 2.status code
     * 3.message type
     * 4.request id
     * 5.service name
     * 6.group name
     */
    private Map<String, Object> attachments;

    /**
     * The RPC version
     */
    private transient byte version;
    /**
     * The local net socket address
     */
    private transient InetSocketAddress localAddress;
    /**
     * The remote net socket address
     */
    private transient InetSocketAddress remoteAddress;

    /**
     * The build new {@link MioMessage}
     * <p>
     * Tips: need set contentLength,metaLength,meta.
     *
     * @param data        body data
     * @param attachments attachments data
     */
    public MioMessage(Object data, Map<String, Object> attachments) {
        this.data = data;
        if (attachments == null || attachments.isEmpty()) {
            this.attachments = new LinkedHashMap<>();
        } else {
            this.attachments = new LinkedHashMap<>(attachments);
        }
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
