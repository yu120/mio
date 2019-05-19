package io.mio;

import io.mio.annotation.MioProtocol;
import io.mio.annotation.MioRegistry;
import io.mio.annotation.MioService;
import io.mio.commons.Assert;
import io.mio.commons.URL;
import io.mio.commons.extension.ExtensionLoader;
import io.mio.config.ApplicationConfig;
import io.mio.config.ProtocolConfig;
import io.mio.config.RegistryConfig;
import io.mio.config.ServiceConfig;
import io.mio.exception.MioFrameException;
import io.mio.model.IProcessor;
import io.mio.model.Request;
import io.mio.model.Response;
import io.mio.register.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The RPC Server Export
 *
 * @author lry
 */
@Slf4j
public class Exporter {

    private static List<URL> registerURLs = new ArrayList<>();
    private static Map<String, IProcessor> serviceProcessorMap = new ConcurrentHashMap<>();
    private static Map<String, ServiceConfig> serviceConfigMap = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Method>> serviceMethodMap = new ConcurrentHashMap<>();
    private static Registry registry;

    /**
     * Publish service
     *
     * @param serviceObj object service instance
     */
    public static void publishService(Object serviceObj) {
        Class<?> clz = serviceObj.getClass();


        // get @MioService Annotation
        if (!clz.isAnnotationPresent(MioService.class)) {
            throw new MioFrameException("The service class must have @" + clz.getSimpleName());
        }
        MioService mioService = clz.getAnnotation(MioService.class);
        if (Assert.isBlank(mioService.group())) {
            throw new MioFrameException("The service group cannot be empty or empty strings");
        }
        if (Assert.isBlank(mioService.version())) {
            throw new MioFrameException("The service version cannot be empty or empty strings");
        }
        // calculation service name
        String serviceName = Assert.isNotBlank(mioService.name()) ? mioService.name() : serviceObj.getClass().getName();
        String serviceId = serviceName + "@" + mioService.group() + "@" + mioService.version();
        if (serviceConfigMap.containsKey(serviceId)) {
            throw new MioFrameException("The service already exist.");
        }


        // get all method names
        List<String> modules = new ArrayList<>();
        Map<String, Method> tempServiceMethodMap = new ConcurrentHashMap<>();
        Method[] methods = clz.getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            throw new MioFrameException("The service version cannot be empty or empty strings");
        }
        for (Method method : methods) {
            if (tempServiceMethodMap.containsKey(method.getName())) {
                throw new MioFrameException("There can be no method with the same method name in a single service");
            }
            modules.add(method.getName());
            tempServiceMethodMap.put(method.getName(), method);
        }
        serviceMethodMap.put(serviceId, tempServiceMethodMap);


        // register service config
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setName(serviceName);
        serviceConfig.setGroup(mioService.group());
        serviceConfig.setVersion(mioService.version());
        serviceConfig.setModules(modules);
        // service registry
        serviceConfig.setRegistry(RegistryConfig.build(clz.getAnnotation(MioRegistry.class)));
        // service protocol
        serviceConfig.setProtocol(ProtocolConfig.build(clz.getAnnotation(MioProtocol.class)));
        serviceConfigMap.put(serviceId, serviceConfig);

        
        // register service processor
        serviceProcessorMap.put(serviceId, request -> doProcessor(serviceId, serviceObj, request));
    }

    /**
     * Export server
     *
     * @param applicationConfig {@link ApplicationConfig}
     * @param registryConfig    {@link RegistryConfig}
     * @param protocolConfig    {@link ProtocolConfig}
     * @return successful return to true
     */
    public static boolean export(ApplicationConfig applicationConfig, RegistryConfig registryConfig, ProtocolConfig protocolConfig) {
        if (serviceConfigMap.isEmpty() || serviceProcessorMap.isEmpty()) {
            throw new MioFrameException("No service can be export.");
        }

        registry = ExtensionLoader.getLoader(Registry.class).getExtension();
        log.debug("Loader registry instance:{}", registry);


        for (Map.Entry<String, ServiceConfig> entry : serviceConfigMap.entrySet()) {
            ServiceConfig serviceConfig = entry.getValue();
            serviceConfig.setApplication(applicationConfig);
            if (serviceConfig.getRegistry() == null) {
                serviceConfig.setRegistry(registryConfig);
            }
            if (serviceConfig.getProtocol() == null) {
                serviceConfig.setProtocol(protocolConfig);
            }

            URL registryURL = entry.getValue().buildURL();
            registerURLs.add(registryURL);
            registry.register(registryURL);
            log.debug("Registry service url:{}", registryURL.toString());
        }

        return true;
    }

    /**
     * Destroy server
     */
    public static void destroy() {
        serviceConfigMap.clear();
        serviceProcessorMap.clear();
    }

    // ===== Internal method

    private static Response doProcessor(String serviceId, Object serviceObj, Request request) {
        Map<String, Method> tempMethodMap = serviceMethodMap.get(serviceId);
        if (tempMethodMap == null || tempMethodMap.isEmpty() || !tempMethodMap.containsKey(request.getModule())) {
            throw new MioFrameException("Service does not exist");
        }
        Method tempMethod = tempMethodMap.get(request.getModule());

        Object returnObj;
        try {
            // TODO: 需更换高性能的动态代理方式
            returnObj = tempMethod.invoke(serviceObj, request.getData());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new MioFrameException("Service invoke exception", e);
        }

        Response response = new Response();
        response.setData(returnObj);
        return response;
    }

}
