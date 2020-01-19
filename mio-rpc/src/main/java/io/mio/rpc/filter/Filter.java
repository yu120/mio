package io.mio.rpc.filter;

import io.mio.core.commons.MioException;
import io.mio.core.extension.SPI;
import io.mio.rpc.MioRpcContext;

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
     * @param context  {@link MioRpcContext}
     * @param request  {@link MioRequest}
     * @param response {@link MioResponse}
     * @throws MioException MioException
     */
    void doFilter(final MioRpcContext context, final MioRequest request, final MioResponse response) throws MioException;

    /**
     * The destroy filter
     */
    default void destroy() {

    }

}
