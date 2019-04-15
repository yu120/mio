package io.mio.register;

import io.mio.commons.URL;

import java.util.List;

/**
 * Subscribe Listener
 *
 * @author lry
 */
public interface SubscribeListener {

    /**
     * Notify service list
     *
     * @param urls
     */
    void notify(List<URL> urls);

}
