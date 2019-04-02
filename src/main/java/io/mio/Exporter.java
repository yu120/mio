package io.mio;

import io.mio.annotation.MioService;
import io.mio.commons.Assert;
import io.mio.config.ApplicationConfig;
import io.mio.config.ProtocolConfig;
import io.mio.config.RegistryConfig;
import io.mio.config.ServiceConfig;
import io.mio.exception.MioFrameException;
import io.mio.model.IProcessor;
import io.mio.model.Response;

import javax.annotation.Resource;
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
@Resource
public class Exporter {

    private static Map<String, ServiceConfig> serviceConfigMap = new ConcurrentHashMap<>();
    private static Map<String, Map<String, Method>> serviceMethodMap = new ConcurrentHashMap<>();
    private static Map<String, IProcessor> serviceProcessorMap = new ConcurrentHashMap<>();

    /**
     * Add service
     *
     * @param serviceObj
     */
    public static void addService(Object serviceObj) {
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
        Method[] methods = clz.getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            throw new MioFrameException("The service version cannot be empty or empty strings");
        }
        List<String> modules = new ArrayList<>();
        Map<String, Method> tempServiceMethodMap = new ConcurrentHashMap<>();
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
        serviceConfigMap.put(serviceId, serviceConfig);

        // register service processor
        serviceProcessorMap.put(serviceId, request -> {
            Map<String, Method> tempMethodMap = serviceMethodMap.get(serviceId);
            if (tempMethodMap == null || tempMethodMap.isEmpty() || !tempMethodMap.containsKey(request.getModule())) {
                throw new MioFrameException("Service does not exist");
            }
            Method tempMethod = tempMethodMap.get(request.getModule());

            Object returnObj = null;
            try {
                returnObj = tempMethod.invoke(serviceObj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new MioFrameException("Service invoke exception", e);
            }

            Response response = new Response();
            response.setData(returnObj);
            return response;
        });
    }

    /**
     * Export server
     *
     * @return successful return to true
     */
    public static boolean export() {
        if (serviceConfigMap.isEmpty() || serviceProcessorMap.isEmpty()) {
            throw new MioFrameException("No service can be export.");
        }

        ApplicationConfig application = new ApplicationConfig();
        ProtocolConfig protocol = new ProtocolConfig();
        RegistryConfig registry = new RegistryConfig();
        for (Map.Entry<String, ServiceConfig> entry : serviceConfigMap.entrySet()) {
            ServiceConfig serviceConfig = entry.getValue();
            serviceConfig.setApplication(application);
            serviceConfig.setProtocol(protocol);
            serviceConfig.setRegistry(registry);
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

}
