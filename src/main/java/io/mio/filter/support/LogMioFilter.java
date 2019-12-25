package io.mio.filter.support;

import io.mio.commons.MioException;
import io.mio.commons.extension.Extension;
import io.mio.filter.FilterContext;
import io.mio.filter.MioFilter;
import io.mio.filter.MioRequest;
import io.mio.filter.MioResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * LogMioFilter
 *
 * @author lry
 */
@Slf4j
@Extension("log")
public class LogMioFilter implements MioFilter {

    @Override
    public void filter(FilterContext context, MioRequest request, MioResponse response) throws MioException {
        log.info("Request:{}, Response:{}", request, response);
    }

}
