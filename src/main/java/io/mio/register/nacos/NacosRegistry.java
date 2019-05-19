package io.mio.register.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import io.mio.commons.URL;
import io.mio.commons.extension.Extension;
import io.mio.commons.thread.NamedThreadFactory;
import io.mio.register.NotifyListener;
import io.mio.register.Constants;
import io.mio.register.support.AbstractFailBackRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Extension("fastjson")
public class NacosRegistry extends AbstractFailBackRegistry {

    private static final String[] ALL_SUPPORTED_CATEGORIES = new String[]{
            Constants.PROVIDERS_CATEGORY,
            Constants.CONSUMERS_CATEGORY,
            Constants.ROUTERS_CATEGORY,
            Constants.CONFIGURATORS_CATEGORY
    };

    private static final int CATEGORY_INDEX = 0;
    private static final int SERVICE_INTERFACE_INDEX = 1;
    private static final int SERVICE_VERSION_INDEX = 2;
    private static final int SERVICE_GROUP_INDEX = 3;
    private static final String WILDCARD = "*";
    private static final String SERVICE_NAME_SEPARATOR = ":";
    private static final int PAGINATION_SIZE = 100;
    private static final long LOOKUP_INTERVAL = 30;
    private final NamingService namingService;
    private volatile ScheduledExecutorService scheduledExecutorService;
    private final ConcurrentMap<String, EventListener> nacosListeners = new ConcurrentHashMap<>();

    public NacosRegistry(URL url, NamingService namingService) {
        super(url);
        this.namingService = namingService;
    }

    @Override
    public boolean isAvailable() {
        return "UP".equals(namingService.getServerStatus());
    }

    @Override
    public List<URL> lookup(final URL url) {
        final List<URL> urls = new LinkedList<>();
        this.execute(namingService -> {
            List<String> serviceNames = this.getServiceNames(url, null);
            for (String serviceName : serviceNames) {
                List<Instance> instances = namingService.getAllInstances(serviceName);
                urls.addAll(buildURLs(url, instances));
            }
        });

        return urls;
    }

    @Override
    public void doRegister(URL url) {
        final String serviceName = this.getServiceName(url);
        final Instance instance = this.createInstance(url);
        this.execute(namingService -> namingService.registerInstance(serviceName, instance));
    }

    @Override
    public void doUnregister(final URL url) {
        this.execute(namingService -> {
            String serviceName = this.getServiceName(url);
            Instance instance = this.createInstance(url);
            namingService.deregisterInstance(serviceName, instance.getIp(), instance.getPort());
        });
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        List<String> serviceNames = this.getServiceNames(url, listener);
        this.doSubscribe(url, listener, serviceNames);
    }

    private void doSubscribe(final URL url, final NotifyListener listener, final List<String> serviceNames) {
        this.execute(namingService -> {
            for (String serviceName : serviceNames) {
                List<Instance> instances = namingService.getAllInstances(serviceName);
                this.notifySubscriber(url, listener, instances);
                this.subscribeEventListener(serviceName, url, listener);
            }
        });
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        if (Constants.ADMIN_PROTOCOL.equals(url.getProtocol())) {
            this.shutdownServiceNamesLookup();
        }
    }

    private void shutdownServiceNamesLookup() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * Get the service names from the specified {@link URL url}
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     * @return non-null
     */
    private List<String> getServiceNames(URL url, NotifyListener listener) {
        if (Constants.ADMIN_PROTOCOL.equals(url.getProtocol())) {
            this.scheduleServiceNamesLookup(url, listener);
            return this.getServiceNamesForOps(url);
        } else {
            return this.doGetServiceNames(url);
        }
    }

    private void scheduleServiceNamesLookup(final URL url, final NotifyListener listener) {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = new ScheduledThreadPoolExecutor(1,
                    new NamedThreadFactory("nacos-registry-"));
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                List<String> serviceNames = this.getAllServiceNames();
                this.filterData(serviceNames, serviceName -> {
                    boolean accepted = false;
                    for (String category : ALL_SUPPORTED_CATEGORIES) {
                        String prefix = category + SERVICE_NAME_SEPARATOR;
                        if (StringUtils.startsWith(serviceName, prefix)) {
                            accepted = true;
                            break;
                        }
                    }
                    return accepted;
                });
                this.doSubscribe(url, listener, serviceNames);
            }, LOOKUP_INTERVAL, LOOKUP_INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * Get the service names for Dubbo OPS
     *
     * @param url {@link URL}
     * @return non-null
     */
    private List<String> getServiceNamesForOps(URL url) {
        List<String> serviceNames = this.getAllServiceNames();
        this.filterServiceNames(serviceNames, url);
        return serviceNames;
    }

    private List<String> getAllServiceNames() {
        final List<String> serviceNames = new LinkedList<>();
        this.execute(namingService -> {
            int pageIndex = 1;
            ListView<String> listView = namingService.getServicesOfServer(pageIndex, PAGINATION_SIZE);
            // First page data
            List<String> firstPageData = listView.getData();
            // Append first page into list
            serviceNames.addAll(firstPageData);
            // the total count
            int count = listView.getCount();
            // the number of pages
            int pageNumbers = count / PAGINATION_SIZE;
            int remainder = count % PAGINATION_SIZE;
            // remain
            if (remainder > 0) {
                pageNumbers += 1;
            }
            // If more than 1 page
            while (pageIndex < pageNumbers) {
                listView = namingService.getServicesOfServer(++pageIndex, PAGINATION_SIZE);
                serviceNames.addAll(listView.getData());
            }
        });

        return serviceNames;
    }

    private void filterServiceNames(List<String> serviceNames, URL url) {
        final String[] categories = getCategories(url);
        final String targetServiceInterface = url.getServiceInterface();
        final String targetVersion = url.getParameter(Constants.VERSION_KEY);
        final String targetGroup = url.getParameter(Constants.GROUP_KEY);
        filterData(serviceNames, serviceName -> {
            // split service name to segments
            // (required) segments[0] = category
            // (required) segments[1] = serviceInterface
            // (required) segments[2] = version
            // (optional) segments[3] = group
            String[] segments = StringUtils.split(serviceName, SERVICE_NAME_SEPARATOR);
            int length = segments.length;
            // must present 3 segments or more
            if (length < 3) {
                return false;
            }

            String category = segments[CATEGORY_INDEX];
            // no match category
            if (!ArrayUtils.contains(categories, category)) {
                return false;
            }

            String serviceInterface = segments[SERVICE_INTERFACE_INDEX];
            if (!WILDCARD.equals(targetServiceInterface) && !StringUtils.equals(targetServiceInterface, serviceInterface)) {
                return false;
            }

            String version = segments[SERVICE_VERSION_INDEX];
            if (!WILDCARD.equals(targetVersion) && !StringUtils.equals(targetVersion, version)) {
                return false;
            }

            String group = length > 3 ? segments[SERVICE_GROUP_INDEX] : null;
            // no match service group
            return group == null || WILDCARD.equals(targetGroup) || StringUtils.equals(targetGroup, group);
        });
    }

    private <T> void filterData(Collection<T> collection, NacosDataFilter<T> filter) {
        // remove if not accept
        collection.removeIf(data -> !filter.accept(data));
    }

    private List<String> doGetServiceNames(URL url) {
        String[] categories = getCategories(url);
        List<String> serviceNames = new ArrayList<>(categories.length);
        for (String category : categories) {
            final String serviceName = this.getServiceName(url, category);
            serviceNames.add(serviceName);
        }
        return serviceNames;
    }

    private List<URL> buildURLs(URL consumerURL, Collection<Instance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        List<URL> urls = new LinkedList<>();
        for (Instance instance : instances) {
            URL url = buildURL(instance);
            if (isMatch(consumerURL, url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private void subscribeEventListener(String serviceName, final URL url, final NotifyListener listener) throws NacosException {
        if (!nacosListeners.containsKey(serviceName)) {
            EventListener eventListener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent e = (NamingEvent) event;
                    this.notifySubscriber(url, listener, e.getInstances());
                }
            };
            namingService.subscribe(serviceName, eventListener);
            nacosListeners.put(serviceName, eventListener);
        }
    }

    /**
     * Notify the Healthy {@link Instance instances} to subscriber.
     *
     * @param url       {@link URL}
     * @param listener  {@link NotifyListener}
     * @param instances all {@link Instance instances}
     */
    private void notifySubscriber(URL url, NotifyListener listener, Collection<Instance> instances) {
        List<Instance> healthyInstances = new LinkedList<>(instances);
        // Healthy Instances
        this.filterHealthyInstances(healthyInstances);
        List<URL> urls = this.buildURLs(url, healthyInstances);
        this.notify(url, listener, urls);
    }

    /**
     * Get the categories from {@link URL}
     *
     * @param url {@link URL}
     * @return non-null array
     */
    private String[] getCategories(URL url) {
        return Constants.ANY_VALUE.equals(url.getServiceInterface()) ?
                ALL_SUPPORTED_CATEGORIES : new String[]{Constants.DEFAULT_CATEGORY};
    }

    private URL buildURL(Instance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String protocol = metadata.get(Constants.PROTOCOL_KEY);
        String path = metadata.get(Constants.PATH_KEY);
        return new URL(protocol, instance.getIp(), instance.getPort(), path, instance.getMetadata());
    }

    private Instance createInstance(URL url) {
        // Append default category if absent
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(Constants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(Constants.PROTOCOL_KEY, url.getProtocol());
        newURL = newURL.addParameter(Constants.PATH_KEY, url.getPath());
        String ip = url.getHost();
        int port = url.getPort();
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
    }

    private String getServiceName(URL url) {
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        return this.getServiceName(url, category);
    }

    private String getServiceName(URL url, String category) {
        StringBuilder serviceNameBuilder = new StringBuilder(category);
        appendIfPresent(serviceNameBuilder, url, Constants.INTERFACE_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.VERSION_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.GROUP_KEY);
        return serviceNameBuilder.toString();
    }

    private void appendIfPresent(StringBuilder target, URL url, String parameterName) {
        String parameterValue = url.getParameter(parameterName);
        if (!StringUtils.isBlank(parameterValue)) {
            target.append(SERVICE_NAME_SEPARATOR).append(parameterValue);
        }
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getErrMsg(), e);
            }
        }
    }

    private void filterHealthyInstances(Collection<Instance> instances) {
        filterData(instances, Instance::isEnabled);
    }

    /**
     * A filter for Nacos data
     */
    private interface NacosDataFilter<T> {

        /**
         * Tests whether or not the specified data should be accepted.
         *
         * @param data The data to be tested
         * @return <code>true</code> if and only if <code>data</code>
         * should be accepted
         */
        boolean accept(T data);

    }

    /**
     * {@link NamingService} Callback
     */
    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         */
        void callback(NamingService namingService) throws NacosException;

    }

}
