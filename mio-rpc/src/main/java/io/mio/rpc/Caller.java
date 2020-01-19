package io.mio.rpc;

import io.mio.rpc.cluster.ClusterFactory;
import io.mio.rpc.filter.FilterChain;
import io.mio.rpc.filter.MioRequest;
import io.mio.rpc.filter.MioResponse;
import io.mio.rpc.registry.DirectoryFactory;

/**
 * Caller(Client)
 *
 * @author lry
 */
public class Caller {

    private DirectoryFactory directoryFactory;
    private ClusterFactory clusterFactory;

    public MioResponse call(MioRequest request) {
        final MioResponse response = new MioResponse();

        final MioRpcContext mioRpcContext = new MioRpcContext();
        mioRpcContext.setFilterChain(FilterChain.INSTANCE);
        mioRpcContext.setRegistries(directoryFactory.discover(request));
        mioRpcContext.setClusters(clusterFactory.select(request));

        mioRpcContext.doFilter(request, response);
        return response;
    }

}
