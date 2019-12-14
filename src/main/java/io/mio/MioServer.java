package io.mio;

import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;
import io.mio.extension.SPI;

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
    void initialize(ServerConfig serverConfig, final MioCallback<MioMessage> mioCallback);

    /**
     * The destroy server
     */
    void destroy();

}
