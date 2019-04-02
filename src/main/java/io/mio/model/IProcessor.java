package io.mio.model;

/**
 * Service Processor
 *
 * @author lry
 */
public interface IProcessor {

    /**
     * Execute service processor
     *
     * @param request {@link Request}
     * @return {@link Response}
     */
    Response processor(Request request);

}
