package io.mio.register;

import io.mio.commons.URL;

import java.util.List;

/**
 * Registry
 *
 * @author lry
 */
public interface Registry {

    /**
     * 注册数据，比如：提供者地址，消费者地址，路由规则，覆盖规则，等数据。
     */
    void register(URL url);

    /**
     * 取消注册.
     */
    void unregister(URL url);

    /**
     * 订阅符合条件的已注册数据，当有注册数据变更时自动推送.
     */
    void subscribe(URL url, NotifyListener listener);

    /**
     * 取消订阅.
     */
    void unsubscribe(URL url, NotifyListener listener);

    /**
     * 查询符合条件的已注册数据，与订阅的推模式相对应，这里为拉模式，只返回一次结果。
     */
    List<URL> lookup(URL url);

    void destroy();
}