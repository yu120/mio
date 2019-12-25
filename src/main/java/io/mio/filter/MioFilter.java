package io.mio.filter;

import io.mio.commons.MioException;
import io.mio.commons.extension.SPI;

/**
 * MioFilter
 *
 * @author lry
 */
@SPI(single = true)
public interface MioFilter {

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
    void filter(final FilterContext context, final MioRequest request, final MioResponse response) throws MioException;

    /**
     * The destroy filter
     */
    default void destroy() {

    }

}
