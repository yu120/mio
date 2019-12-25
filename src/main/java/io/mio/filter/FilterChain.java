package io.mio.filter;

import io.mio.commons.MioException;
import io.mio.commons.extension.Extension;
import io.mio.commons.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterChain
 *
 * @author lry
 */
public class FilterChain {

    private List<Filter> filters = new ArrayList<>();

    /**
     * The initialize filter
     */
    public void initialize() {
        List<Filter> scanFilters = ExtensionLoader.getLoader(Filter.class).getExtensions();
        if (!scanFilters.isEmpty()) {
            // sort by order filter
            scanFilters.sort((o1, o2) -> {
                Extension e1 = o1.getClass().getAnnotation(Extension.class);
                Extension e2 = o2.getClass().getAnnotation(Extension.class);
                return e1.order() - e2.order();
            });

            // initialize and add filter
            for (Filter scanFilter : scanFilters) {
                scanFilter.initialize();
                filters.add(scanFilter);
            }
        }
    }

    /**
     * The invoke filter
     *
     * @param context  {@link FilterContext}
     * @param request  {@link MioRequest}
     * @param response {@link MioResponse}
     * @throws MioException MioException
     */
    public void filter(final FilterContext context, final MioRequest request, final MioResponse response) throws MioException {
        for (Filter filter : filters) {
            filter.filter(context, request, response);
        }
    }

    /**
     * The destroy filter
     */
    public void destroy() {
        for (Filter filter : filters) {
            // destroy filter
            filter.destroy();
        }
    }

}
