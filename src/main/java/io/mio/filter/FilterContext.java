package io.mio.filter;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioException;
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
    private ClientConfig clientConfig;

    /**
     * The initialize filter
     *
     * @param filterChain  {@link FilterChain}
     * @param clientConfig {@link ClientConfig}
     */
    public void initialize(FilterChain filterChain, ClientConfig clientConfig) {
        this.filterSize = filterChain.getFilters().size();
        this.index = new AtomicInteger(0);
        this.filters = filterChain.getFilters();
        this.clientConfig = clientConfig;
    }

    /**
     * The invoke filter
     *
     * @param context  {@link FilterContext}
     * @param request  {@link MioRequest}
     * @param response {@link MioResponse}
     * @throws MioException MioException
     */
    public void doFilter(final FilterContext context, final MioRequest request, final MioResponse response) throws MioException {
        int currentIndex = index.getAndIncrement();
        if (currentIndex == filters.size()) {
            return;
        }

        Filter currentFilter = filters.get(currentIndex);
        currentFilter.doFilter(context, request, response);
    }

}
