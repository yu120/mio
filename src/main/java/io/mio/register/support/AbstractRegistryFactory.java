package io.mio.register.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import io.mio.commons.URL;
import io.mio.register.Registry;
import io.mio.register.RegistryFactory;
import io.mio.register.Constants;
import lombok.extern.slf4j.Slf4j;


/**
 * AbstractRegistryFactory
 *
 * @author lry
 */
@Slf4j
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static final ReentrantLock LOCK = new ReentrantLock();
    /**
     * 注册中心集合 Map<RegistryAddress, Registry>
     */
    private static final Map<String, Registry> REGISTRIES = new ConcurrentHashMap<>();

    /**
     * 获取所有注册中心
     *
     * @return 所有注册中心
     */
    public static Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

    /**
     * 关闭所有已创建注册中心
     */
    public static void destroyAll() {
        log.info("Close all registries " + getRegistries());
        // 锁定注册中心关闭过程
        LOCK.lock();
        try {
            for (Registry registry : getRegistries()) {
                try {
                    registry.destroy();
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
            REGISTRIES.clear();
        } finally {
            // 释放锁
            LOCK.unlock();
        }
    }

    @Override
    public Registry getRegistry(URL url) {
        url.setPath(Registry.class.getName());
        url = url.addParameter(Constants.INTERFACE_KEY, Registry.class.getName());
        String key = url.getServiceKey();

        // 锁定注册中心获取过程，保证注册中心单一实例
        LOCK.lock();
        try {
            Registry registry = REGISTRIES.get(key);
            if (registry != null) {
                return registry;
            }
            registry = this.createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }
            REGISTRIES.put(key, registry);
            return registry;
        } finally {
            // 释放锁
            LOCK.unlock();
        }
    }

    /**
     * Create registry
     *
     * @param url {@link URL}
     * @return {@link Registry}
     */
    protected abstract Registry createRegistry(URL url);

}