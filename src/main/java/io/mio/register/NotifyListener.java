package io.mio.register;

import io.mio.commons.URL;

import java.util.List;

/**
 * Notify Listener
 *
 * @author lry
 */
public interface NotifyListener {

    /**
     * 当收到服务变更通知时触发
     *
     * @param urls 已注册的全部列表，总不为空
     */
    void notify(List<URL> urls);

}