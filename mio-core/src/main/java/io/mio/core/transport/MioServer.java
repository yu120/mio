package io.mio.core.transport;

import io.mio.core.MioMessage;
import io.mio.core.MioProcessor;
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
     * @param mioProcessor  {@link MioProcessor}
     */
    void initialize(final ServerConfig serverConfig, final MioProcessor<MioMessage> mioProcessor);

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
