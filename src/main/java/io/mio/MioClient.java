package io.mio;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import io.mio.commons.MioMessageFuture;
import io.mio.commons.extension.SPI;

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
    void initialize(ClientConfig clientConfig);

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
     * @return {@link MioMessageFuture}
     * @throws Throwable exception {@link Throwable}
     */
    MioMessageFuture<MioMessage> submit(final MioMessage mioMessage) throws Throwable;

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
