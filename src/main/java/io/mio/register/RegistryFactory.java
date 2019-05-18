package io.mio.register;

import io.mio.commons.URL;

public interface RegistryFactory {

    /**
     * 连接注册中心.
     *
     * @param url 注册中心地址，不允许为空
     * @return 注册中心引用，总不返回空
     */
    Registry getRegistry(URL url);

}