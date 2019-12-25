package io.mio.filter;

import io.mio.commons.extension.Extension;
import io.mio.commons.extension.ExtensionLoader;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterChain
 *
 * @author lry
 */
@Getter
public enum FilterChain {

    // ===

    INSTANCE;

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
     * The destroy filter
     */
    public void destroy() {
        for (Filter filter : filters) {
            // destroy filter
            filter.destroy();
        }
    }

}
