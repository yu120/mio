package io.mio.transport.netty4;

import io.mio.core.extension.SPI;
import io.mio.core.transport.ClientConfig;
import io.mio.core.transport.ServerConfig;

/**
 * NettyInitializer
 *
 * @author lry
 */
@SPI("mio")
public interface NettyInitializer<A> {

    /**
     * The server decode and encode
     *
     * @param serverConfig {@link ServerConfig}
     * @param attachment   {@link A}
     */
    void server(ServerConfig serverConfig, A attachment);

    /**
     * The client decode and encode
     *
     * @param clientConfig {@link ClientConfig}
     * @param attachment   {@link A}
     */
    void client(ClientConfig clientConfig, A attachment);

}
