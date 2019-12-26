package io.mio.rpc;

import io.mio.rpc.filter.FilterChain;
import io.mio.rpc.filter.FilterContext;
import io.mio.rpc.filter.MioRequest;
import io.mio.rpc.filter.MioResponse;
import io.mio.rpc.registry.Registry;
import io.mio.rpc.registry.RegistryDirectory;

import java.util.List;

/**
 * Caller(Client)
 *
 * @author lry
 */
public class Caller {

    private RegistryDirectory registryDirectory;

    public MioResponse call(MioRequest request) {
        final MioResponse response = new MioResponse();

        final List<Registry> registries = registryDirectory.discover(request);

        final FilterContext filterContext = new FilterContext(FilterChain.INSTANCE, registries);
        filterContext.doFilter(request, response);
        return response;
    }

}
