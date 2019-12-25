package io.mio.filter;

import io.mio.commons.MioException;
import io.mio.commons.extension.SPI;

/**
 * Filter
 *
 * @author lry
 */
@SPI(single = true)
public interface Filter {

    /**
     * The initialize filter
     */
    default void initialize() {

    }

    /**
     * The invoke filter
     *
     * @param context  {@link FilterContext}
     * @param request  {@link MioRequest}
     * @param response {@link MioResponse}
     * @throws MioException MioException
     */
    void doFilter(final FilterContext context, final MioRequest request, final MioResponse response) throws MioException;

    /**
     * The destroy filter
     */
    default void destroy() {

    }

}
