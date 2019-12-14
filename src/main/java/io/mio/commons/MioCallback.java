package io.mio.commons;

import java.util.function.Consumer;

/**
 * A callback for accepting the results of a computation asynchronously.
 *
 * @param <V> {@link V}
 * @author lry
 */
public class MioCallback<V> {

    /**
     * The operation function
     */
    private Consumer<V> listener;

    /**
     * The setter operation function
     *
     * @param listener {@link Consumer}
     * @return {@link MioCallback}
     */
    public final MioCallback<V> setListener(Consumer<V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * The notify operation function
     *
     * @return {@link MioCallback}
     */
    public final MioCallback<V> notifyListener() {
        listener.accept(null);
        return this;
    }

    /**
     * Asynchronous notification after client success.
     *
     * @param result result
     */
    public void onSuccess(V result) {

    }

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
     * @param consumer {@link Consumer<V>}
     * @param result   result
     */
    public void onProcessor(Consumer<V> consumer, V result) {

    }

}
