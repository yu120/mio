package io.mio;

import io.mio.commons.ClientConfig;
import io.mio.commons.ServerConfig;
import io.mio.commons.extension.ExtensionLoader;

/**
 * MioTransport
 *
 * @author lry
 */
public class MioTransport {

    /**
     * The create server
     *
     * @param serverConfig {@link ServerConfig}
     * @return {@link MioServer}
     */
    public static MioServer createServer(ServerConfig serverConfig) {
        return ExtensionLoader.getLoader(MioServer.class).getExtension(serverConfig.getTransport());
    }

    /**
     * The create client
     *
     * @param clientConfig {@link ClientConfig}
     * @return {@link MioClient}
     */
    public static MioClient createClient(ClientConfig clientConfig) {
        return ExtensionLoader.getLoader(MioClient.class).getExtension(clientConfig.getTransport());
    }

}
