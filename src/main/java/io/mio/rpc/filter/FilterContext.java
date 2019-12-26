package io.mio.rpc.filter;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioException;
import io.mio.rpc.registry.Registry;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FilterContext
 *
 * @author lry
 */
@Data
public class FilterContext implements Serializable {

    private int filterSize;
    private AtomicInteger index;
    private List<Filter> filters;
    private List<Registry> registries;

    /**
     * The initialize filter
     *
     * @param filterChain {@link FilterChain}
     * @param registries  {@link List<Registry>}
     */
    public FilterContext(FilterChain filterChain, List<Registry> registries) {
        this.filterSize = filterChain.getFilters().size();
        this.index = new AtomicInteger(0);
        this.filters = filterChain.getFilters();
        this.registries = registries;
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
