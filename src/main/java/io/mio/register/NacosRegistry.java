package io.mio.register;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.mio.commons.URL;
import io.mio.commons.URLParamType;
import io.mio.commons.extension.Extension;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Nacos Service Registry
 *
 * @author lry
 */
@Slf4j
@Extension("nacos")
public class NacosRegistry implements IRegistry {

    private NamingService namingService;
    private ConcurrentMap<String, EventListener> eventListeners = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ConcurrentMap<String, List<SubscribeListener>>> subscribeListeners = new ConcurrentHashMap<>();

    @Override
    public void initialize(URL url) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, url.buildServerAddress());
        if (url.getParameters().containsKey(PropertyKeyConst.NAMESPACE)) {
            properties.setProperty(PropertyKeyConst.NAMESPACE, url.getParameter(PropertyKeyConst.NAMESPACE));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.ENDPOINT)) {
            properties.setProperty(PropertyKeyConst.ENDPOINT, url.getParameter(PropertyKeyConst.ENDPOINT));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.ACCESS_KEY)) {
            properties.setProperty(PropertyKeyConst.ACCESS_KEY, url.getParameter(PropertyKeyConst.ACCESS_KEY));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.SECRET_KEY)) {
            properties.setProperty(PropertyKeyConst.SECRET_KEY, url.getParameter(PropertyKeyConst.SECRET_KEY));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.CLUSTER_NAME)) {
            properties.setProperty(PropertyKeyConst.CLUSTER_NAME, url.getParameter(PropertyKeyConst.CLUSTER_NAME));
        }

        try {
            this.namingService = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void register(URL url) {
        Instance instance = this.buildInstance(url);

        try {
            namingService.registerInstance(instance.getServiceName(), instance);
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void unRegister(URL url) {
        try {
            namingService.deregisterInstance(url.getServiceName(), url.getHost(), url.getPort());
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void subscribe(URL url, SubscribeListener subscribeListener) {
        // first notify
        subscribeListener.notify(this.selectHealthyInstances(url));

        // event listener already exist
        ConcurrentMap<String, List<SubscribeListener>> subscribeListenerMap = subscribeListeners.get(url.getServiceName());
        EventListener eventListener = eventListeners.get(url.getServiceName());
        if (subscribeListenerMap != null && eventListener != null) {
            subscribeListenerMap.computeIfAbsent(url.getServiceNameIdentity(), e -> new ArrayList<>()).add(subscribeListener);
            return;
        }

        // cache subscribe listener
        subscribeListeners.put(url.getServiceName(), subscribeListenerMap = new ConcurrentHashMap<>());
        subscribeListenerMap.computeIfAbsent(url.getServiceNameIdentity(), e -> new ArrayList<>()).add(subscribeListener);

        // cache event listener
        eventListeners.put(url.getServiceName(), eventListener = event -> {
            if (event instanceof NamingEvent) {
                // Map<serviceName Identity, List < URL>>
                Map<String, List<URL>> tempGroupUrlMap = this.selectGroupHealthyInstances(url.getServiceName());
                log.debug("Subscribe event listener is notify:{}", tempGroupUrlMap);
                for (Map.Entry<String, List<URL>> entry : tempGroupUrlMap.entrySet()) {
                    try {
                        List<SubscribeListener> tempSubscribeListeners = subscribeListeners.get(url.getServiceNameIdentity()).get(entry.getKey());
                        for (SubscribeListener tempSubscribeListener : tempSubscribeListeners) {
                            try {
                                tempSubscribeListener.notify(entry.getValue());
                            } catch (Exception e) {
                                log.error("The notify[" + tempSubscribeListener.getClass().getName() + "] is fail", e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("The notifies[" + entry.getKey() + "->" + entry.getValue() + "] is fail", e);
                    }
                }
            }
        });

        try {
            // subscribe service
            namingService.subscribe(url.getServiceName(), eventListener);
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void unSubscribe(URL url, SubscribeListener subscribeListener) {
        ConcurrentMap<String, List<SubscribeListener>> subscribeListenerMap = subscribeListeners.get(url.getServiceName());
        if (subscribeListenerMap == null) {
            return;
        }

        List<SubscribeListener> subscribeListenerList = subscribeListenerMap.get(url.getServiceNameIdentity());
        if (subscribeListenerList == null) {
            return;
        }

        // cancel subscribe all listener
        if (subscribeListener == null) {
            subscribeListenerMap.clear();
            subscribeListeners.remove(url.getServiceName());
            eventListeners.remove(url.getServiceName());
            return;
        }

        subscribeListenerList.remove(subscribeListener);
        if (subscribeListenerMap.isEmpty()) {
            EventListener eventListener = eventListeners.get(url.getServiceName());
            if (eventListener == null) {
                return;
            }

            subscribeListeners.remove(url.getServiceName());
            eventListeners.remove(url.getServiceName());

            try {
                namingService.unsubscribe(url.getServiceName(), eventListener);
            } catch (NacosException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<URL> select(URL url) {
        return this.selectHealthyInstances(url);
    }

    @Override
    public void destroy() {
        if (subscribeListeners != null) {
            subscribeListeners.clear();
        }
        if (eventListeners != null) {
            eventListeners.clear();
        }
    }

    /**
     * Select healthy instances
     *
     * @param url {@link URL}
     * @return {@link List<URL>}
     */
    private List<URL> selectHealthyInstances(URL url) {
        String selectServiceNameIdentity = url.getServiceNameIdentity();

        try {
            List<Instance> instances = namingService.selectInstances(url.getServiceName(), true);
            if (instances == null || instances.isEmpty()) {
                return Collections.emptyList();
            }

            List<URL> dstUrls = new ArrayList<>();
            List<URL> allUrls = this.parseInstances(instances);
            for (URL tempUrl : allUrls) {
                if (selectServiceNameIdentity.equals(tempUrl.getServiceNameIdentity())) {
                    dstUrls.add(tempUrl);
                }
            }

            return dstUrls;
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Select group healthy instances
     *
     * @param serviceName service name
     * @return Map<serviceName Identity, List < URL>>
     */
    private Map<String, List<URL>> selectGroupHealthyInstances(String serviceName) {

        try {
            List<Instance> instances = namingService.selectInstances(serviceName, true);
            if (instances == null || instances.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, List<URL>> dstUrlMap = new ConcurrentHashMap<>();
            List<URL> allUrls = this.parseInstances(instances);
            for (URL tempUrl : allUrls) {
                dstUrlMap.computeIfAbsent(tempUrl.getServiceNameIdentity(), k -> new ArrayList<>()).add(tempUrl);
            }

            return dstUrlMap;
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    /**
     * Build instances
     *
     * @param urls {@link List<URL>}
     * @return {@link List<Instance>}
     */
    private List<Instance> buildInstances(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return Collections.emptyList();
        }

        List<Instance> instances = new ArrayList<>();
        for (URL url : urls) {
            instances.add(this.buildInstance(url));
        }

        return instances;
    }

    /**
     * Build instance
     *
     * @param url {@link URL}
     * @return {@link Instance}
     */
    private Instance buildInstance(URL url) {
        Map<String, String> metadata = new HashMap<>(url.getParameters());
        metadata.put(URLParamType.PROTOCOL.getName(), url.getProtocol());
        metadata.put(URLParamType.GROUP.getName(), url.getGroup());
        metadata.put(URLParamType.VERSION.getName(), url.getVersion());

        Instance instance = new Instance();
        instance.setInstanceId(url.getHost() + "@" + URLParamType.PID.getValue());
        instance.setIp(url.getHost());
        instance.setPort(url.getPort());
        instance.setWeight(url.getWeight());
        instance.setHealthy(url.getHealthy());
        instance.setEnabled(url.getEnabled());
        instance.setServiceName(url.getServiceName());
        instance.setMetadata(metadata);
        return instance;
    }

    /**
     * Parse instances
     *
     * @param instances {@link List<Instance>}
     * @return {@link List<URL>}
     */
    private List<URL> parseInstances(List<Instance> instances) {
        if (instances == null || instances.isEmpty()) {
            return Collections.emptyList();
        }

        List<URL> urls = new ArrayList<>();
        for (Instance instance : instances) {
            urls.add(this.parseInstance(instance));
        }

        return urls;
    }

    /**
     * Parse instance
     *
     * @param instance {@link Instance}
     * @return {@link URL}
     */
    private URL parseInstance(Instance instance) {
        Map<String, String> metadata = new HashMap<>(instance.getMetadata());
        String protocol = metadata.get(URLParamType.PROTOCOL.getName());
        metadata.remove(URLParamType.PROTOCOL.getName());
        return new URL(protocol, instance.getIp(), instance.getPort(), instance.getServiceName(), metadata);
    }

}
