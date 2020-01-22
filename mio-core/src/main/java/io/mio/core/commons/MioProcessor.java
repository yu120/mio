package io.mio.core.commons;

import java.util.function.Consumer;

/**
 * MioProcessor
 *
 * @param <V>
 * @author lry
 */
public class MioProcessor<V> {

    /**
     * Asynchronous notification after server/client failure
     *
     * @param t {@link Throwable}
     */
    public void onFailure(Throwable t) {

    }

    /**
     * Asynchronous notification after server success.
     *
     * @param context request context {@link Consumer <V>}
     * @param request request message
     */
    public void onProcessor(Consumer<V> context, V request) {

    }

}
