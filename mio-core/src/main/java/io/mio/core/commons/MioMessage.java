package io.mio.core.commons;

import io.mio.core.serialize.Serialize;
import io.mio.core.utils.ExceptionUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

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
     * The exception data
     */
    private Exception exception;
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
     * @param attachments attachments data
     * @param data        body data
     */
    public MioMessage(Map<String, Object> attachments, byte[] data) {
        if (attachments == null || attachments.isEmpty()) {
            this.attachments = new LinkedHashMap<>();
        } else {
            this.attachments = new LinkedHashMap<>(attachments);
        }
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

    public byte[] toBody(Function<Object, byte[]> function) {
        if (exception == null) {
            return (data instanceof byte[]) ? (byte[]) data : function.apply(data);
        } else {
            return ExceptionUtils.toString(exception).getBytes(StandardCharsets.UTF_8);
        }
    }

}
