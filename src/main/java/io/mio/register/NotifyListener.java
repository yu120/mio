package io.mio.register;

import io.mio.commons.URL;

import java.util.List;

public interface NotifyListener {

    /**
     * 当收到服务变更通知时触发
     *
     * @param urls 已注册信息列表，总不为空
     */
    void notify(List<URL> urls);

}