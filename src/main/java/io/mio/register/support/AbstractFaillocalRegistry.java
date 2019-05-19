package io.mio.register.support;

import io.mio.commons.ConcurrentHashSet;
import io.mio.commons.URL;
import io.mio.commons.thread.NamedThreadFactory;
import io.mio.register.Constants;
import io.mio.register.NotifyListener;
import io.mio.register.Registry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract Fail Local Registry
 *
 * @author lry
 */
@Slf4j
@Getter
public abstract class AbstractFaillocalRegistry implements Registry {

    // === 分隔符

    private static final char URL_SEPARATOR = ' ';
    private static final String URL_SPLIT = "\\s+";

    private URL url;
    private File file;
    private final boolean syncSaveFile;
    private final Properties properties = new Properties();
    private final AtomicLong lastCacheChanged = new AtomicLong();
    private final Set<URL> registered = new ConcurrentHashSet<>();
    private final ConcurrentMap<URL, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();
    private final ConcurrentMap<URL, Map<String, List<URL>>> notified = new ConcurrentHashMap<>();
    private final ExecutorService registryCacheExecutor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("save-registry-cache", true));


    public AbstractFaillocalRegistry(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("registry url == null");
        }
        this.url = url;
        // 启动文件保存定时器
        this.syncSaveFile = url.getParameter(Constants.REGISTRY_FILESAVE_SYNC_KEY, false);
        String filename = url.getParameter(Constants.FILE_KEY,
                System.getProperty("user.home") + "/.mio/mio-registry-" + url.getHost() + ".cache");
        File file = null;
        if (!StringUtils.isEmpty(filename)) {
            file = new File(filename);
            if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid registry store file " +
                            file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                }
            }
        }
        this.file = file;
        loadProperties();
        notify(url.getBackupUrls());
    }

    private class SaveProperties implements Runnable {
        private long version;

        private SaveProperties(long version) {
            this.version = version;
        }

        @Override
        public void run() {
            doSaveProperties(version);
        }
    }

    private void doSaveProperties(long version) {
        if (version < lastCacheChanged.get()) {
            return;
        }
        if (file == null) {
            return;
        }
        Properties newProperties = new Properties();
        // 保存之前先读取一遍，防止多个注册中心之间冲突
        InputStream in = null;
        try {
            if (file.exists()) {
                in = new FileInputStream(file);
                newProperties.load(in);
            }
        } catch (Throwable e) {
            log.warn("Failed to load registry store file, cause: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        // 保存
        try {
            newProperties.putAll(properties);
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                if (!lockfile.createNewFile()) {
                    throw new RuntimeException("Create file fail");
                }
            }

            try (RandomAccessFile raf = new RandomAccessFile(lockfile, "rw")) {
                try (FileChannel channel = raf.getChannel()) {
                    FileLock lock = channel.tryLock();
                    if (lock == null) {
                        throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath() +
                                ", ignore and retry later, maybe multi java process use the file, " +
                                "please config: registry.file=xxx.properties");
                    }

                    // === 保存

                    try {
                        if (!file.exists()) {
                            if (!file.createNewFile()) {
                                throw new RuntimeException("Create file fail");
                            }
                        }
                        try (FileOutputStream outputFile = new FileOutputStream(file)) {
                            newProperties.store(outputFile, "Ms Registry Cache");
                        }
                    } finally {
                        lock.release();
                    }
                }
            }
        } catch (Throwable e) {
            if (version < lastCacheChanged.get()) {
                return;
            }

            registryCacheExecutor.execute(new SaveProperties(lastCacheChanged.incrementAndGet()));
            log.warn("Failed to save registry store file, cause: " + e.getMessage(), e);
        }
    }

    private void loadProperties() {
        if (file != null && file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
                log.info("Load registry store file " + file + ", data: " + properties);
            } catch (Throwable e) {
                log.warn("Failed to load registry store file " + file, e);
            }
        }
    }

    List<URL> getCacheUrls(URL url) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key != null && key.length() > 0 && key.equals(url.getServiceKey())
                    && (Character.isLetter(key.charAt(0)) || key.charAt(0) == '_')
                    && value != null && value.length() > 0) {
                String[] arr = value.trim().split(URL_SPLIT);
                List<URL> urls = new ArrayList<>();
                for (String u : arr) {
                    urls.add(URL.valueOf(u));
                }
                return urls;
            }
        }
        return null;
    }

    @Override
    public List<URL> lookup(URL url) {
        List<URL> result = new ArrayList<>();
        Map<String, List<URL>> notifiedUrls = getNotified().get(url);
        if (notifiedUrls != null && notifiedUrls.size() > 0) {
            for (List<URL> urls : notifiedUrls.values()) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        } else {
            final AtomicReference<List<URL>> reference = new AtomicReference<>();
            // 订阅逻辑保证第一次notify后再返回
            subscribe(url, reference::set);
            List<URL> urls = reference.get();
            if (urls != null && urls.size() > 0) {
                for (URL u : urls) {
                    if (!Constants.EMPTY_PROTOCOL.equals(u.getProtocol())) {
                        result.add(u);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void register(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("register url == null");
        }
        if (log.isInfoEnabled()) {
            log.info("Register: " + url);
        }
        registered.add(url);
    }

    @Override
    public void unregister(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("unregister url == null");
        }
        if (log.isInfoEnabled()) {
            log.info("Unregister: " + url);
        }
        registered.remove(url);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("subscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("subscribe listener == null");
        }
        if (log.isInfoEnabled()) {
            log.info("Subscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners == null) {
            subscribed.putIfAbsent(url, new ConcurrentHashSet<>());
            listeners = subscribed.get(url);
        }
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(URL url, NotifyListener listener) {
        if (url == null) {
            throw new IllegalArgumentException("unsubscribe url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("unsubscribe listener == null");
        }
        if (log.isInfoEnabled()) {
            log.info("Unsubscribe: " + url);
        }
        Set<NotifyListener> listeners = subscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * 恢复注册和订阅
     *
     * @throws Exception throw exception
     */
    protected void recover() throws Exception {
        // register
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Recover register url " + recoverRegistered);
            }
            for (URL url : recoverRegistered) {
                register(url);
            }
        }

        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    subscribe(url, listener);
                }
            }
        }
    }

    private void notify(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }

        for (Map.Entry<URL, Set<NotifyListener>> entry : getSubscribed().entrySet()) {
            if (!isMatch(entry.getKey(), urls.get(0))) {
                continue;
            }
            Set<NotifyListener> listeners = entry.getValue();
            if (listeners != null) {
                for (NotifyListener listener : listeners) {
                    try {
                        notify(entry.getKey(), listener, filterEmpty(entry.getKey(), urls));
                    } catch (Throwable t) {
                        log.error("Failed to notify registry event, urls: " + urls + ", cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    /**
     * 过滤不为空的 {@link URL}
     *
     * @param url  url
     * @param urls url list
     * @return not empty url list
     */
    private static List<URL> filterEmpty(URL url, List<URL> urls) {
        if (urls == null || urls.size() == 0) {
            List<URL> result = new ArrayList<>(1);
            url.setProtocol(Constants.EMPTY_PROTOCOL);
            result.add(url);
            return result;
        }
        return urls;
    }

    /**
     * 通知
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     * @param urls     {@link List}
     */
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        if (urls == null || urls.size() == 0
                || !Constants.ANY_VALUE.equals(url.getServiceInterface())) {
            log.warn("Ignore empty notify urls for subscribe url " + url);
            return;
        }

        log.info("Notify urls for subscribe url " + url + ", urls: " + urls);
        Map<String, List<URL>> result = new HashMap<>();
        for (URL u : urls) {
            if (isMatch(url, u)) {
                String category = u.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
                result.computeIfAbsent(category, k -> new ArrayList<>()).add(u);
            }
        }
        if (result.size() == 0) {
            return;
        }
        Map<String, List<URL>> categoryNotified = notified.get(url);
        if (categoryNotified == null) {
            notified.putIfAbsent(url, new ConcurrentHashMap<>());
            categoryNotified = notified.get(url);
        }
        for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
            String category = entry.getKey();
            List<URL> categoryList = entry.getValue();
            categoryNotified.put(category, categoryList);
            saveProperties(url);
            listener.notify(categoryList);
        }
    }

    private void saveProperties(URL url) {
        if (file == null) {
            return;
        }

        try {
            StringBuilder buf = new StringBuilder();
            Map<String, List<URL>> categoryNotified = notified.get(url);
            if (categoryNotified != null) {
                for (List<URL> us : categoryNotified.values()) {
                    for (URL u : us) {
                        if (buf.length() > 0) {
                            buf.append(URL_SEPARATOR);
                        }
                        buf.append(u.toString());
                    }
                }
            }
            properties.setProperty(url.getServiceKey(), buf.toString());
            long version = lastCacheChanged.incrementAndGet();
            if (syncSaveFile) {
                doSaveProperties(version);
            } else {
                registryCacheExecutor.execute(new SaveProperties(version));
            }
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroy registry:" + getUrl());
        // destroy registered
        Set<URL> destroyRegistered = new HashSet<>(getRegistered());
        if (!destroyRegistered.isEmpty()) {
            for (URL url : new HashSet<>(getRegistered())) {
                if (!url.getParameter(Constants.DYNAMIC_KEY, true)) {
                    continue;
                }

                try {
                    unregister(url);
                    log.info("Destroy unregister url " + url);
                } catch (Throwable t) {
                    log.warn("Failed to unregister url " + url + " to registry " +
                            getUrl() + " on destroy, cause: " + t.getMessage(), t);
                }
            }
        }

        // destroy subscribed
        Map<URL, Set<NotifyListener>> destroySubscribed = new HashMap<>(getSubscribed());
        if (!destroySubscribed.isEmpty()) {
            for (Map.Entry<URL, Set<NotifyListener>> entry : destroySubscribed.entrySet()) {
                for (NotifyListener listener : entry.getValue()) {
                    try {
                        unsubscribe(entry.getKey(), listener);
                        log.info("Destroy unsubscribe url " + entry.getKey());
                    } catch (Throwable t) {
                        log.warn("Failed to unsubscribe url " + entry.getKey() + " to registry " +
                                getUrl() + " on destroy, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return getUrl().toString();
    }

    public static boolean isMatch(URL consumerUrl, URL providerUrl) {
        String consumerInterface = consumerUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (!(Constants.ANY_VALUE.equals(consumerInterface) || consumerInterface.equals(providerInterface))) {
            return false;
        }

        if (!isMatchCategory(providerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY),
                consumerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY))) {
            return false;
        }
        if (!providerUrl.getParameter(Constants.ENABLED_KEY, true)
                && !Constants.ANY_VALUE.equals(consumerUrl.getParameter(Constants.ENABLED_KEY))) {
            return false;
        }

        String consumerGroup = consumerUrl.getParameter(Constants.GROUP_KEY);
        String consumerVersion = consumerUrl.getParameter(Constants.VERSION_KEY);
        String consumerClassifier = consumerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);

        String providerGroup = providerUrl.getParameter(Constants.GROUP_KEY);
        String providerVersion = providerUrl.getParameter(Constants.VERSION_KEY);
        String providerClassifier = providerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);
        return (Constants.ANY_VALUE.equals(consumerGroup) ||
                consumerGroup.equals(providerGroup) ||
                isContains(consumerGroup, providerGroup))
                && (Constants.ANY_VALUE.equals(consumerVersion) ||
                consumerVersion.equals(providerVersion))
                && (consumerClassifier == null ||
                Constants.ANY_VALUE.equals(consumerClassifier) ||
                consumerClassifier.equals(providerClassifier));
    }

    private static boolean isMatchCategory(String category, String categories) {
        if (categories == null || categories.length() == 0) {
            return Constants.DEFAULT_CATEGORY.equals(category);
        } else if (categories.contains(Constants.ANY_VALUE)) {
            return true;
        } else if (categories.contains(Constants.REMOVE_VALUE_PREFIX)) {
            return !categories.contains(Constants.REMOVE_VALUE_PREFIX + category);
        } else {
            return categories.contains(category);
        }
    }

    private static boolean isContains(String values, String value) {
        if (values == null || values.length() == 0) {
            return false;
        }
        String[] tempValues = Constants.COMMA_SPLIT_PATTERN.split(values);
        if (value != null && value.length() > 0 && tempValues != null && tempValues.length > 0) {
            for (String v : tempValues) {
                if (value.equals(v)) {
                    return true;
                }
            }
        }

        return false;
    }

}