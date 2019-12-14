package io.mio;

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
     * Header length
     */
    private int headerLength;
    /**
     * Data length(Header data length + Data length)
     */
    private int dataLength;

    /**
     * The header parameters
     */
    private Map<String, Object> headers;
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

    private MioMessage() {

    }

    public static MioMessage buildEmpty() {
        return new MioMessage();
    }

    /**
     * The build new {@link MioMessage}
     * <p>
     * Tips: need set contentLength,metaLength,meta.
     *
     * @param headers header parameters
     * @param data    data byte[]
     * @return {@link MioMessage}
     */
    public static MioMessage build(Map<String, Object> headers, byte[] data) {
        MioMessage mioMessage = new MioMessage();
        mioMessage.setData(data);
        if (headers != null && !headers.isEmpty()) {
            mioMessage.setHeaders(new LinkedHashMap<>(headers));
        }
        return mioMessage;
    }

    /**
     * The build new {@link MioMessage}
     *
     * @param headers      header parameters
     * @param headerLength header length
     * @param dataLength   data length
     * @param data         data byte[]
     * @return {@link MioMessage}
     */
    public static MioMessage build(Map<String, Object> headers, int headerLength, int dataLength, byte[] data) {
        MioMessage mioMessage = new MioMessage();
        mioMessage.setHeaderLength(headerLength);
        mioMessage.setDataLength(dataLength);
        if (headers != null && !headers.isEmpty()) {
            mioMessage.setHeaders(new LinkedHashMap<>(headers));
        }
        mioMessage.setData(data);
        return mioMessage;
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
