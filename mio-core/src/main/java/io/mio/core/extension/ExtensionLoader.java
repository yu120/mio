package io.mio.core.extension;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The Extension Loader
 *
 * @param <T>
 * @author lry
 */
@Slf4j
public class ExtensionLoader<T> {

    private Class<T> type;
    private ClassLoader classLoader;
    private volatile boolean init = false;
    private ConcurrentMap<String, T> singletonInstances = null;
    private ConcurrentMap<String, Class<T>> extensionClasses = null;
    private static ConcurrentMap<Class<?>, ExtensionLoader<?>> extensionLoaders = new ConcurrentHashMap<>();
    private static final List<String> PREFIX_LIST = Arrays.asList("META-INF/", "META-INF/mio/", "META-INF/services/");
    private static final List<Class<? extends Annotation>> ANNOTATION_LIST = new ArrayList<>(Collections.singletonList(Extension.class));

    public static void addExtensionAnnotation(Class<? extends Annotation> annotationClass) {
        ANNOTATION_LIST.add(annotationClass);
    }

    private ExtensionLoader(Class<T> type, ClassLoader classLoader) {
        this.type = type;
        this.classLoader = classLoader;
    }

    private void checkInit() {
        if (!init) {
            loadExtensionClasses();
        }
    }

    public Class<T> getExtensionClass(String name) {
        this.checkInit();
        return extensionClasses.get(name);
    }

    public T getExtension() {
        this.checkInit();

        SPI spi = type.getAnnotation(SPI.class);
        if (spi.value().length() == 0) {
            throw new RuntimeException(type.getName() + ": The default implementation ID(@SPI.value()) is not set");
        } else {
            try {
                if (spi.single()) {
                    return this.getSingletonInstance(spi.value());
                } else {
                    Class<T> clz = extensionClasses.get(spi.value());
                    if (clz == null) {
                        return null;
                    }

                    return clz.newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException(type.getName() + ": Error when getExtension ", e);
            }
        }
    }

    public T getExtension(String name) {
        this.checkInit();
        if (name == null) {
            return null;
        }

        try {
            SPI spi = type.getAnnotation(SPI.class);
            if (spi.single()) {
                return this.getSingletonInstance(name);
            } else {
                Class<T> clz = extensionClasses.get(name);
                if (clz == null) {
                    return null;
                }

                return clz.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(type.getName() + ": Error when getExtension ", e);
        }
    }

    private T getSingletonInstance(String name) throws InstantiationException, IllegalAccessException {
        T obj = singletonInstances.get(name);
        if (obj != null) {
            return obj;
        }

        Class<T> clz = extensionClasses.get(name);
        if (clz == null) {
            return null;
        }

        synchronized (singletonInstances) {
            obj = singletonInstances.get(name);
            if (obj != null) {
                return obj;
            }
            obj = clz.newInstance();
            singletonInstances.put(name, obj);
        }

        return obj;
    }

    public void addExtensionClass(Class<T> clz) {
        if (clz == null) {
            return;
        }

        checkInit();
        checkExtensionType(clz);
        String spiName = getSpiName(clz);
        synchronized (extensionClasses) {
            if (extensionClasses.containsKey(spiName)) {
                throw new RuntimeException(clz.getName() + ": Error spiName already exist " + spiName);
            } else {
                extensionClasses.put(spiName, clz);
            }
        }
    }

    private synchronized void loadExtensionClasses() {
        if (init) {
            return;
        }
        if (extensionClasses == null) {
            extensionClasses = new ConcurrentHashMap<>();
        }

        for (String prefix : PREFIX_LIST) {
            ConcurrentMap<String, Class<T>> tempExtensionClasses = this.loadExtensionClasses(prefix);
            if (!tempExtensionClasses.isEmpty()) {
                extensionClasses.putAll(tempExtensionClasses);
            }
        }

        singletonInstances = new ConcurrentHashMap<>();
        init = true;
    }

    public static <T> ExtensionLoader<T> getLoader(Class<T> type) {
        return getLoader(type, Thread.currentThread().getContextClassLoader());
    }

    public static <T> ExtensionLoader<T> getLoader(TypeReference<T> type) {
        return getLoader(type, Thread.currentThread().getContextClassLoader());
    }

    public static <T> ExtensionLoader<T> getLoader(TypeReference<T> type, ClassLoader classLoader) {
        return getLoader(type.getClassType(), classLoader);
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getLoader(Class<T> type, ClassLoader classLoader) {
        if (type == null) {
            throw new RuntimeException("Error extension type is null");
        }
        if (!type.isAnnotationPresent(SPI.class)) {
            throw new RuntimeException(type.getName() + ": Error extension type without @SPI annotation");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) extensionLoaders.get(type);
        if (loader == null) {
            loader = initExtensionLoader(type, classLoader);
        }

        return loader;
    }

    public static synchronized <T> ExtensionLoader<T> initExtensionLoader(Class<T> type) {
        return initExtensionLoader(type, Thread.currentThread().getContextClassLoader());
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> ExtensionLoader<T> initExtensionLoader(Class<T> type, ClassLoader classLoader) {
        ExtensionLoader<T> loader = (ExtensionLoader<T>) extensionLoaders.get(type);
        if (loader == null) {
            loader = new ExtensionLoader<T>(type, classLoader);
            extensionLoaders.putIfAbsent(type, loader);
            loader = (ExtensionLoader<T>) extensionLoaders.get(type);
        }

        return loader;
    }

    public List<T> getExtensions() {
        return this.getExtensions("");
    }

    public List<T> getExtensions(String key) {
        return getExtensions(key, Extension.class);
    }

    /**
     * 有些地方需要spi的所有激活的instances，所以需要能返回一个列表的方法<br>
     */
    public <A extends Annotation> List<T> getExtensions(String key, Class<A> annotationClass) {
        checkInit();
        if (extensionClasses.size() == 0) {
            return Collections.emptyList();
        }

        // 如果只有一个实现，直接返回
        List<T> extList = new ArrayList<T>(extensionClasses.size());
        // 多个实现，按优先级排序返回
        for (Map.Entry<String, Class<T>> entry : extensionClasses.entrySet()) {
            A extension = entry.getValue().getAnnotation(annotationClass);
            if (key == null || key.length() == 0) {
                extList.add(getExtension(entry.getKey()));
            } else if (extension != null) {
                Object category;
                try {
                    category = annotationClass.getMethod("category").invoke(extension);
                } catch (Exception e) {
                    throw new RuntimeException("Not found:" + key, e);
                }

                if (category instanceof String[]) {
                    String[] categories = (String[]) category;
                    for (String k : categories) {
                        if (key.equals(k)) {
                            extList.add(getExtension(entry.getKey()));
                            break;
                        }
                    }
                }
            }
        }

        // order 大的排在后面,如果没有设置order的排到最前面
        try {
            Method method = annotationClass.getMethod("order");
            if (method != null) {
                extList.sort((o1, o2) -> {
                    A p1 = o1.getClass().getAnnotation(annotationClass);
                    A p2 = o2.getClass().getAnnotation(annotationClass);
                    if (p1 == null) {
                        return 1;
                    } else if (p2 == null) {
                        return -1;
                    } else {
                        try {
                            int p1Order = (int) method.invoke(p1);
                            int p2Order = (int) method.invoke(p2);
                            return p1Order - p2Order;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return extList;
    }

    private void checkExtensionType(Class<T> clz) {
        // 1) is public class
        if (!type.isAssignableFrom(clz)) {
            throw new RuntimeException(clz.getName() + ": Error is not instanceof " + type.getName());
        }

        // 2) contain public constructor and has not-args constructor
        Constructor<?>[] constructors = clz.getConstructors();
        if (constructors.length == 0) {
            throw new RuntimeException(clz.getName() + ": Error has no public no-args constructor");
        }

        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPublic(constructor.getModifiers()) && constructor.getParameterTypes().length == 0) {
                // 3) check extension clz instanceof Type.class
                if (!type.isAssignableFrom(clz)) {
                    throw new RuntimeException(clz.getName() + ": Error is not instanceof " + type.getName());
                }

                return;
            }
        }

        throw new RuntimeException(clz.getName() + ": Error has no public no-args constructor");
    }

    private ConcurrentMap<String, Class<T>> loadExtensionClasses(String prefix) {
        String fullName = prefix + type.getName();
        List<String> classNames = new ArrayList<>();

        try {
            Enumeration<URL> urls;
            if (classLoader == null) {
                urls = ClassLoader.getSystemResources(fullName);
            } else {
                urls = classLoader.getResources(fullName);
            }

            if (urls == null || !urls.hasMoreElements()) {
                return new ConcurrentHashMap<>();
            }

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                this.parseUrl(type, url, classNames);
            }
        } catch (Exception e) {
            throw new RuntimeException("ExtensionLoader loadExtensionClasses error, prefix: " +
                    prefix + " type: " + type, e);
        }

        return loadClass(classNames);
    }

    @SuppressWarnings("unchecked")
    private ConcurrentMap<String, Class<T>> loadClass(List<String> classNames) {
        ConcurrentMap<String, Class<T>> map = new ConcurrentHashMap<>();
        for (String className : classNames) {
            try {
                Class<T> clz;
                if (classLoader == null) {
                    clz = (Class<T>) Class.forName(className);
                } else {
                    clz = (Class<T>) Class.forName(className, true, classLoader);
                }

                this.checkExtensionType(clz);
                String spiName = this.getSpiName(clz);
                if (map.containsKey(spiName)) {
                    throw new RuntimeException(clz.getName() + ": Error spiName already exist " + spiName);
                } else {
                    map.put(spiName, clz);
                }
            } catch (Exception e) {
                log.error(type.getName() + ": Error load spi class", e);
            }
        }

        return map;

    }

    private String getSpiName(Class<?> clz) {
        for (Class<? extends Annotation> annotationClass : ANNOTATION_LIST) {
            Annotation extension = clz.getAnnotation(annotationClass);
            if (extension == null) {
                continue;
            }

            try {
                Method method = annotationClass.getMethod("value");
                if (method == null) {
                    continue;
                }

                Object value = method.invoke(extension);
                if (value == null || "".equals(String.valueOf(value))) {
                    continue;
                }

                return String.valueOf(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return clz.getSimpleName();
    }

    private void parseUrl(Class<T> type, URL url, List<String> classNames) throws ServiceConfigurationError {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = url.openStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                this.parseLine(type, url, line, classNames);
            }
        } catch (Exception e) {
            log.error(type.getName() + ": Error reading spi configuration file", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error(type.getName() + ": Error closing spi configuration file", e);
            }
        }
    }

    private void parseLine(Class<T> type, URL url, String line, List<String> names) throws ServiceConfigurationError {
        int ci = line.indexOf('#');
        if (ci >= 0) {
            line = line.substring(0, ci);
        }

        line = line.trim();
        if (line.length() <= 0) {
            return;
        }
        if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
            throw new RuntimeException(type.getName() + ": " +
                    url + ":" + line + ": Illegal spi configuration-file syntax: " + line);
        }

        int cp = line.codePointAt(0);
        if (!Character.isJavaIdentifierStart(cp)) {
            throw new RuntimeException(type.getName() + ": " +
                    url + ":" + line + ": Illegal spi provider-class name: " + line);
        }

        for (int i = Character.charCount(cp); i < line.length(); i += Character.charCount(cp)) {
            cp = line.codePointAt(i);
            if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                throw new RuntimeException(type.getName() + ": " +
                        url + ":" + line + ": Illegal spi provider-class name: " + line);
            }
        }

        if (!names.contains(line)) {
            names.add(line);
        }
    }

    /**
     * TypeReference
     *
     * @param <T>
     * @author lry
     */
    public static abstract class TypeReference<T> {

        private Class<T> classType;

        @SuppressWarnings("unchecked")
        public TypeReference() {
            Type superClass = this.getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                Type parameterizedType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                if (parameterizedType instanceof ParameterizedType) {
                    classType = (Class<T>) ((ParameterizedType) parameterizedType).getRawType();
                } else {
                    classType = (Class<T>) parameterizedType;
                }
            }
        }

        public Class<T> getClassType() {
            return classType;
        }

    }

}
