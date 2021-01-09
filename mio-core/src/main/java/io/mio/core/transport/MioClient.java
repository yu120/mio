package io.mio.core.transport;

import io.mio.core.MioCallback;
import io.mio.core.MioFuture;
import io.mio.core.MioMessage;
import io.mio.core.extension.SPI;

/**
 * MioClient
 *
 * @author lry
 */
@SPI("netty")
public interface MioClient {

    /**
     * The initialize client
     *
     * @param clientConfig {@link ClientConfig}
     */
    void initialize(final ClientConfig clientConfig);

    /**
     * The send request
     *
     * @param message {@link MioMessage}
     * @return {@link MioMessage}
     * @throws Throwable exception {@link Throwable}
     */
    MioMessage request(final MioMessage message) throws Throwable;

    /**
     * The send submit
     *
     * @param message {@link MioMessage}
     * @return {@link MioFuture}
     * @throws Throwable exception {@link Throwable}
     */
    MioFuture<MioMessage> submit(final MioMessage message) throws Throwable;

    /**
     * The send callback
     *
     * @param message  {@link MioMessage}
     * @param callback {@link MioCallback}
     * @throws Throwable exception {@link Throwable}
     */
    void callback(final MioMessage message, final MioCallback<MioMessage> callback) throws Throwable;

    /**
     * The destroy client
     */
    void destroy();

}
