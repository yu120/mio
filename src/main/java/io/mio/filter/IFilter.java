package io.mio.filter;

import io.mio.FilterChain;
import io.mio.model.Request;
import io.mio.model.Response;

/**
 * Filter
 *
 * @author lry
 */
public interface IFilter {

    Response filter(FilterChain filterChain, Request request);

}
