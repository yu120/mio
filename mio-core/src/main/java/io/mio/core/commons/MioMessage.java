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
 * 1.sever success: 2x
 * 2.service error: 3x
 * 3.client error: 4x
 * 4.server error: 5x
 * <p>
 * Tip: MIO Unified message
 *
 * @author lry
 */
@Data
@NoArgsConstructor
public class MioMessage implements Serializable {

    // === sever success: 2x

    /**
     * ok.
     */
    public static final byte OK = 20;

    // === service error: 3x

    /**
     * service error.
     */
    public static final byte SERVICE_ERROR = 30;
    /**
     * service not found.
     */
    public static final byte SERVICE_NOT_FOUND = 31;
    /**
     * server side thread pool rejected.
     */
    public static final byte THREAD_POOL_REJECTED = 32;

    // === client error: 4x

    /**
     * internal server error.
     */
    public static final byte CLIENT_ERROR = 40;
    /**
     * client side timeout.
     */
    public static final byte CLIENT_TIMEOUT = 41;
    /**
     * channel inactive, directly return the unfinished requests.
     */
    public static final byte CHANNEL_INACTIVE = 42;
    /**
     * request format error.
     */
    public static final byte BAD_REQUEST = 43;

    // === server error: 5x

    /**
     * internal server error.
     */
    public static final byte SERVER_ERROR = 50;
    /**
     * server side timeout.
     */
    public static final byte SERVER_TIMEOUT = 51;
    /**
     * response format error.
     */
    public static final byte BAD_RESPONSE = 52;


    /**
     * The status code
     */
    private byte code = OK;
    /**
     * The error message
     */
    private String error;
    /**
     * The body data
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

    public MioMessage(byte code, String error) {
        this.code = code;
        this.error = error;
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
