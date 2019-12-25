package io.mio;

import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;
import io.mio.commons.extension.SPI;

/**
 * MioServer
 *
 * @author lry
 */
@SPI("netty")
public interface MioServer {

    /**
     * The initialize server
     *
     * @param serverConfig {@link ServerConfig}
     * @param mioCallback  {@link MioCallback}
     */
    void initialize(final ServerConfig serverConfig, final MioCallback<MioMessage> mioCallback);

    /**
     * The send request
     *
     * @param mioMessage {@link MioMessage}
     * @throws Throwable exception {@link Throwable}
     */
    void send(final MioMessage mioMessage) throws Throwable;

    /**
     * The destroy server
     */
    void destroy();

}
