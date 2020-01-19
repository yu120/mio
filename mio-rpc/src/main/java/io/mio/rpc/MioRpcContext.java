package io.mio.rpc;

import io.mio.core.commons.MioException;
import io.mio.rpc.cluster.Cluster;
import io.mio.rpc.filter.Filter;
import io.mio.rpc.filter.FilterChain;
import io.mio.rpc.filter.MioRequest;
import io.mio.rpc.filter.MioResponse;
import io.mio.rpc.registry.Registry;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MioRpcContext
 *
 * @author lry
 */
@Data
public class MioRpcContext implements Serializable {

    private int filterSize;
    private AtomicInteger index;
    private List<Filter> filters;

    private List<Registry> registries;
    private Registry remoteRegistry;

    private List<Cluster> clusters;
    private Cluster cluster;

    /**
     * The initialize filter
     *
     * @param filterChain {@link FilterChain}
     */
    public void setFilterChain(FilterChain filterChain) {
        this.filterSize = filterChain.getFilters().size();
        this.filters = filterChain.getFilters();
        this.index = new AtomicInteger(0);
    }

    /**
     * The invoke filter
     *
     * @param request  {@link MioRequest}
     * @param response {@link MioResponse}
     * @throws MioException MioException
     */
    public void doFilter(final MioRequest request, final MioResponse response) throws MioException {
        int currentIndex = index.getAndIncrement();
        if (currentIndex == filters.size()) {
            return;
        }

        Filter currentFilter = filters.get(currentIndex);
        currentFilter.doFilter(this, request, response);
    }

}
