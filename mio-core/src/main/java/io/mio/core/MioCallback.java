package io.mio.core;

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
    public final MioCallback<V> listener(Consumer<V> listener) {
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
     * @param response response message
     */
    public void onSuccess(V response) {

    }

    /**
     * Asynchronous notification after server/client failure
     *
     * @param t {@link Throwable}
     */
    public void onFailure(Throwable t) {

    }

}
