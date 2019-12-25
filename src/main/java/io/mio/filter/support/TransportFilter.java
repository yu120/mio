package io.mio.filter.support;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioException;
import io.mio.commons.MioMessage;
import io.mio.commons.extension.Extension;
import io.mio.filter.Filter;
import io.mio.filter.FilterContext;
import io.mio.filter.MioRequest;
import io.mio.filter.MioResponse;
import io.mio.transport.MioClient;
import io.mio.transport.MioTransport;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TransportFilter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "transport", order = 5000)
public class TransportFilter implements Filter {

    private ConcurrentMap<String, MioClient> clients = new ConcurrentHashMap<>();

    @Override
    public void doFilter(FilterContext context, MioRequest request, MioResponse response) throws MioException {
        MioClient mioClient = getAndCreateClient(context.getClientConfig());
        MioMessage requestMioMessage = new MioMessage(request.getHeaders(), request.getData());

        try {
            MioMessage responseMioMessage = mioClient.request(requestMioMessage);

            // build response
            response.setHeaders(responseMioMessage.getHeaders());
            response.setData(responseMioMessage.getData());
        } catch (Throwable t) {
            throw new MioException(0, "Transport request exception", t);
        }

        context.doFilter(context, request, response);
    }

    @Override
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
