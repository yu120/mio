package io.mio.register.support;

import io.mio.commons.ConcurrentHashSet;
import io.mio.commons.URL;
import io.mio.commons.thread.NamedThreadFactory;
import io.mio.register.Constants;
import io.mio.register.NotifyListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;

/**
 * Fail back Registry
 *
 * @author lry
 */
@Slf4j
@Getter
public abstract class AbstractFailBackRegistry extends AbstractFailLocalRegistry {

    private final ScheduledFuture<?> retryFuture;
    private final ScheduledExecutorService retryExecutor = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("registry-failed-retry-timer", true));

    private final Set<URL> failedRegistered = new ConcurrentHashSet<>();
    private final Set<URL> failedUnregistered = new ConcurrentHashSet<>();
    private final ConcurrentMap<URL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Set<NotifyListener>> failedUnsubscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Map<NotifyListener, List<URL>>> failedNotified = new ConcurrentHashMap<>();

    public AbstractFailBackRegistry(URL url) {
        super(url);
        int retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryFuture = retryExecutor.scheduleWithFixedDelay(() -> {
            try {
                // 检测并连接注册中心
                this.doFailRetry();
            } catch (Throwable t) {
                // 防御性容错
                log.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

    private void addFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(url, new ConcurrentHashSet<>());
            listeners = failedSubscribed.get(url);
        }
        listeners.add(listener);
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnsubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener, List<URL>> notified = failedNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    @Override
    public void register(URL url) {
        super.register(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);

        try {
            // 向服务器端发送注册请求
            this.doRegister(url);
        } catch (Exception e) {
            // 如果开启了启动时检测，则直接抛出异常
            if (getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol())) {
                throw new IllegalStateException("Failed to register " + url + " to registry "
                        + getUrl().getAddress() + ", cause: " + e.getMessage(), e);
            } else {
                log.error("Failed to register " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            }

            // 将失败的注册请求记录到失败列表，定时重试
            failedRegistered.add(url);
        }
    }

    @Override
    public void unregister(URL url) {
        super.unregister(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);

        try {
            // 向服务器端发送取消注册请求
            this.doUnregister(url);
        } catch (Exception e) {
            // 如果开启了启动时检测，则直接抛出异常
            if (getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol())) {
                throw new IllegalStateException("Failed to unregister " + url + " to registry "
                        + getUrl().getAddress() + ", cause: " + e.getMessage(), e);
            } else {
                log.error("Failed to uregister " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            }

            // 将失败的取消注册请求记录到失败列表，定时重试
            failedUnregistered.add(url);
        }
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        super.subscribe(url, listener);
        this.removeFailedSubscribed(url, listener);

        try {
            // 向服务器端发送订阅请求
            this.doSubscribe(url, listener);
        } catch (Exception e) {
            List<URL> urls = super.getCacheUrls(url);
            if (urls != null && urls.size() > 0) {
                this.notify(url, listener, urls);
                log.error("Failed to subscribe " + url + ", Using cached list: " + urls + ", cause: " + e.getMessage(), e);
            } else {
                // 如果开启了启动时检测，则直接抛出异常
                if (getUrl().getParameter(Constants.CHECK_KEY, true)
                        && url.getParameter(Constants.CHECK_KEY, true)) {
                    throw new IllegalStateException("Failed to subscribe " + url + ", cause: " + e.getMessage(), e);
                } else {
                    log.error("Failed to subscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
                }
            }

            // 将失败的订阅请求记录到失败列表，定时重试
            this.addFailedSubscribed(url, listener);
        }
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        super.unsubscribe(url, listener);
        this.removeFailedSubscribed(url, listener);

        try {
            // 向服务器端发送取消订阅请求
            this.doUnsubscribe(url, listener);
        } catch (Exception e) {
            // 如果开启了启动时检测，则直接抛出异常
            if (getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)) {
                throw new IllegalStateException("Failed to unsubscribe " + url + " to registry "
                        + getUrl().getAddress() + ", cause: " + e.getMessage(), e);
            } else {
                log.error("Failed to unsubscribe " + url + ", waiting for retry, cause: " + e.getMessage(), e);
            }

            // 将失败的取消订阅请求记录到失败列表，定时重试
            Set<NotifyListener> listeners = failedUnsubscribed.get(url);
            if (listeners == null) {
                failedUnsubscribed.putIfAbsent(url, new ConcurrentHashSet<>());
                listeners = failedUnsubscribed.get(url);
            }
            listeners.add(listener);
        }
    }

    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            this.doNotify(url, listener, urls);
        } catch (Exception t) {
            // 将失败的通知请求记录到失败列表，定时重试
            Map<NotifyListener, List<URL>> listeners = failedNotified.get(url);
            if (listeners == null) {
                failedNotified.putIfAbsent(url, new ConcurrentHashMap<>());
                listeners = failedNotified.get(url);
            }
            listeners.put(listener, urls);
            log.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    private void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }

    @Override
    protected void recover() throws Exception {
        super.recover();

        // register
        Set<URL> recoverRegistered = new HashSet<>(this.getRegistered());
        if (!recoverRegistered.isEmpty()) {
            log.info("Recover register url: {}", recoverRegistered);
            failedRegistered.addAll(recoverRegistered);
        }
        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(this.getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            log.info("Recover subscribe url: {}", recoverSubscribed.keySet());
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    this.addFailedSubscribed(url, listener);
                }
            }
        }
    }

    /**
     * 重试失败的动作
     * <p>
     * Step 1: retry failed registered
     * Step 2: retry failed unregistered
     * Step 3: retry failed subscribed
     * Step 4: retry failed unsubscribed
     * Step 5: retry failed notified
     */
    private void doFailRetry() {
        // Step 1: retry failed registered
        if (!failedRegistered.isEmpty()) {
            Set<URL> failed = new HashSet<>(failedRegistered);
            if (failed.size() > 0) {
                log.info("Retry register: {}", failed);
                try {
                    for (URL url : failed) {
                        try {
                            this.doRegister(url);
                            failedRegistered.remove(url);
                        } catch (Throwable t) {
                            // 忽略所有异常，等待下次重试
                            log.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) {
                    // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry register " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }

        // Step 2: retry failed unregistered
        if (!failedUnregistered.isEmpty()) {
            Set<URL> failed = new HashSet<>(failedUnregistered);
            if (failed.size() > 0) {
                log.info("Retry unregister: {}", failed);
                try {
                    for (URL url : failed) {
                        try {
                            this.doUnregister(url);
                            failedUnregistered.remove(url);
                        } catch (Throwable t) {
                            // 忽略所有异常，等待下次重试
                            log.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                        }
                    }
                } catch (Throwable t) {
                    // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry unregister  " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }

        // Step 3: retry failed subscribed
        if (!failedSubscribed.isEmpty()) {
            Map<URL, Set<NotifyListener>> failed = new HashMap<>(failedSubscribed);
            for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                log.info("Retry subscribe: {}", failed);
                try {
                    for (Map.Entry<URL, Set<NotifyListener>> entry : failed.entrySet()) {
                        URL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                this.doSubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) {
                                // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) {
                    // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }

        // Step 4: retry failed unsubscribed
        if (!failedUnsubscribed.isEmpty()) {
            Map<URL, Set<NotifyListener>> failed = new HashMap<>(failedUnsubscribed);
            for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                log.info("Retry unsubscribe: {}", failed);
                try {
                    for (Map.Entry<URL, Set<NotifyListener>> entry : failed.entrySet()) {
                        URL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                this.doUnsubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) {
                                // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) {
                    // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }

        // Step 5: retry failed notified
        if (!failedNotified.isEmpty()) {
            Map<URL, Map<NotifyListener, List<URL>>> failed = new HashMap<>(failedNotified);
            for (Map.Entry<URL, Map<NotifyListener, List<URL>>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.size() > 0) {
                log.info("Retry notify: {}", failed);
                try {
                    for (Map<NotifyListener, List<URL>> values : failed.values()) {
                        for (Map.Entry<NotifyListener, List<URL>> entry : values.entrySet()) {
                            try {
                                NotifyListener listener = entry.getKey();
                                List<URL> urls = entry.getValue();
                                listener.notify(urls);
                                values.remove(listener);
                            } catch (Throwable t) {
                                // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) {
                    // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            retryExecutor.shutdown();
            retryFuture.cancel(true);
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
    }

    // ==== 模板方法 ====

    /**
     * Handler register
     *
     * @param url {@link URL}
     */
    protected abstract void doRegister(URL url);

    /**
     * Handler cancel register
     *
     * @param url {@link URL}
     */
    protected abstract void doUnregister(URL url);

    /**
     * Handler subscribe
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     */
    protected abstract void doSubscribe(URL url, NotifyListener listener);

    /**
     * Handler cancel subscribe
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     */
    protected abstract void doUnsubscribe(URL url, NotifyListener listener);

}