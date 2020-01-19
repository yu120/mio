package io.mio.core.transport;

import io.mio.core.commons.ClientConfig;
import io.mio.core.commons.MioCallback;
import io.mio.core.commons.MioFuture;
import io.mio.core.commons.MioMessage;
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
     * @param mioMessage {@link MioMessage}
     * @return {@link MioMessage}
     * @throws Throwable exception {@link Throwable}
     */
    MioMessage request(final MioMessage mioMessage) throws Throwable;

    /**
     * The send submit
     *
     * @param mioMessage {@link MioMessage}
     * @return {@link MioFuture}
     * @throws Throwable exception {@link Throwable}
     */
    MioFuture<MioMessage> submit(final MioMessage mioMessage) throws Throwable;

    /**
     * The send callback
     *
     * @param mioMessage  {@link MioMessage}
     * @param mioCallback {@link MioCallback}
     * @throws Throwable exception {@link Throwable}
     */
    void callback(final MioMessage mioMessage, final MioCallback<MioMessage> mioCallback) throws Throwable;

    /**
     * The destroy client
     */
    void destroy();

}
