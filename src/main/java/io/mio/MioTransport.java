package io.mio;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
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
     * @param mioCallback  {@link MioCallback<MioMessage>}
     * @return {@link MioServer}
     */
    public static MioServer createServer(final ServerConfig serverConfig, final MioCallback<MioMessage> mioCallback) {
        MioServer mioServer = ExtensionLoader.getLoader(MioServer.class).getExtension(serverConfig.getTransport());
        mioServer.initialize(serverConfig, mioCallback);
        return mioServer;
    }

    /**
     * The create client
     *
     * @param clientConfig {@link ClientConfig}
     * @return {@link MioClient}
     */
    public static MioClient createClient(ClientConfig clientConfig) {
        MioClient mioClient = ExtensionLoader.getLoader(MioClient.class).getExtension(clientConfig.getTransport());
        mioClient.initialize(clientConfig);
        return mioClient;
    }

}