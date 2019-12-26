package io.mio.rpc.filter.support;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioException;
import io.mio.commons.extension.Extension;
import io.mio.rpc.MioRpcContext;
import io.mio.rpc.filter.Filter;
import io.mio.rpc.filter.MioRequest;
import io.mio.rpc.filter.MioResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * TransportFilter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "load-balance", order = 4000)
public class LoadBalanceFilter implements Filter {

    @Override
    public void doFilter(MioRpcContext context, MioRequest request, MioResponse response) throws MioException {
        try {
            // setter LoadBalance config
            context.setClientConfig(new ClientConfig());
        } catch (Throwable t) {
            throw new MioException(0, "LoadBalance request exception", t);
        }

        context.doFilter(request, response);
    }

}
