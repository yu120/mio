package io.mio.register;

import io.mio.commons.URL;

import java.util.List;

/**
 * The Registry Service
 *
 * @author lry
 */
public interface IRegistry {

    /**
     * Initialize registry
     *
     * @param url {@link URL}
     */
    void initialize(URL url);

    /**
     * Register service
     *
     * @param url {@link URL}
     */
    void register(URL url);

    /**
     * Cancel registry service
     *
     * @param url {@link URL}
     */
    void unRegister(URL url);

    /**
     * Subscribe service
     *
     * @param url               {@link URL}
     * @param subscribeListener {@link SubscribeListener}
     */
    void subscribe(URL url, SubscribeListener subscribeListener);

    /**
     * Cancel subscribe service
     *
     * @param url {@link URL}
     */
    void unSubscribe(URL url);

    /**
     * Select service list
     *
     * @param url {@link URL}
     * @return {@link List<URL>}
     */
    List<URL> select(URL url);

    /**
     * Destroy registry
     */
    void destroy();

}
