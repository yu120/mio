package io.mio.core.transport;

import io.mio.core.commons.MioCallback;
import io.mio.core.commons.MioMessage;
import io.mio.core.commons.ServerConfig;
import io.mio.core.extension.SPI;

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
