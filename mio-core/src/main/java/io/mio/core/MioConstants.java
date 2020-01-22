package io.mio.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MioConstants
 *
 * @author lry
 */
public class MioConstants {

    /**
     * Command head data(Fixed: 0x76)
     */
    public static final byte MAGIC_DATA = 0x76;

    /**
     * Magic length(byte，1 byte)
     */
    private static final int MAGIC_BYTE = 1;
    /**
     * Version length(byte，1 byte)
     */
    private static final int VERSION_BYTE = 1;
    /**
     * Data length(int，4 byte）
     */
    private static final int LENGTH_BYTE = 4;
    /**
     * Xor byte length(byte, 1 byte)
     */
    public static final int XOR_BYTE = 1;
    /**
     * Read basic length (read only after reaching)
     */
    public static final int BASE_READ_LENGTH = MAGIC_BYTE + VERSION_BYTE + LENGTH_BYTE;

    /**
     * HTTP uri key
     */
    public static final String URI_KEY = "uri";
    /**
     * HTTP query parameters key
     */
    public static final String PARAMETERS_KEY = "parameters";
    /**
     * HTTP request method key
     */
    public static final String REQUEST_METHOD_KEY = "method";
    /**
     * HTTP response status key
     */
    public static final String RESPONSE_STATUS_KEY = "status";

    /**
     * The new {@link ThreadFactory} instance
     *
     * @param name   thread name
     * @param daemon thread daemon
     * @return {@link ThreadFactory}
     */
    public static ThreadFactory newThreadFactory(String name, boolean daemon) {
        return new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name + "-pool-" + index.getAndIncrement());
                t.setDaemon(daemon);
                return t;
            }
        };
    }

    /**
     * The build {@link SocketAddress}
     *
     * @param hostname hostname
     * @param port     port
     * @return {@link SocketAddress}
     */
    public static SocketAddress buildSocketAddress(String hostname, int port) {
        if (hostname == null || hostname.trim().length() == 0) {
            return new InetSocketAddress(port);
        } else {
            return new InetSocketAddress(hostname, port);
        }
    }

    /**
     * The get {@link SocketAddress} key
     *
     * @param socketAddress {@link SocketAddress}
     * @return {@link SocketAddress} key
     */
    public static String getSocketAddressKey(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        if (inetSocketAddress == null || inetSocketAddress.getAddress() == null) {
            return "null";
        } else {
            return inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
        }
    }

}
