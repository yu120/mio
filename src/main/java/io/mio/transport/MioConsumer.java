package io.mio.transport;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioException;
import io.mio.commons.MioMessage;
import io.mio.rpc.filter.FilterContext;
import io.mio.rpc.filter.MioRequest;
import io.mio.rpc.filter.MioResponse;
import io.mio.serialize.Serialize;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MioConsumer
 *
 * @author lry
 */
@Slf4j
public class MioConsumer {

    private Serialize serialize;
    private ConcurrentMap<String, MioClient> clients = new ConcurrentHashMap<>();

    public void consumer(FilterContext context, MioRequest request, MioResponse response) throws MioException {
        MioClient mioClient = getAndCreateClient(null);

        MioMessage requestMessage = new MioMessage(request.getHeaders(), null, null);
        wrapperSerialize(request, requestMessage);

        try {
            MioMessage responseMessage = mioClient.request(requestMessage);
            wrapperDeserialize(response, responseMessage);
        } catch (Throwable t) {
            throw new MioException(0, "Transport request exception", t);
        }

        context.doFilter(request, response);
    }

    public void destroy() {
        for (Map.Entry<String, MioClient> entry : clients.entrySet()) {
            try {
                MioClient mioClient = entry.getValue();
                mioClient.destroy();
            } catch (Exception e) {
                log.error("Destroy client[{}] exception", entry.getKey(), e);
            }
        }
    }

    private void wrapperSerialize(MioRequest request, MioMessage message) {
        try {
            message.setHeader(serialize.serialize(request.getHeaders()));
            message.setData(serialize.serialize(request.getData()));
        } catch (Exception e) {
            throw new MioException(0, "serialize exception", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void wrapperDeserialize(MioResponse response, MioMessage message) {
        try {
            response.setHeaders(serialize.deserialize(message.getHeader(), Map.class));
            response.setData(serialize.deserialize(message.getData(), null));
        } catch (Exception e) {
            throw new MioException(0, "serialize exception", e);
        }
    }

    /**
     * The get and create client
     *
     * @param clientConfig {@link ClientConfig}
     * @return {@link MioClient}
     */
    private MioClient getAndCreateClient(ClientConfig clientConfig) {
        String remoteAddress = String.format("%s:%s", clientConfig.getHostname(), clientConfig.getPort());
        MioClient mioClient = clients.get(remoteAddress);
        if (mioClient == null) {
            clients.put(remoteAddress, mioClient = MioTransport.createClient(clientConfig));
        }

        return mioClient;
    }

}
