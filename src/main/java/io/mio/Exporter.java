package io.mio;

import io.mio.exception.MioFrameException;
import io.mio.model.IProcessor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The RPC Server Export
 *
 * @author lry
 */
public class Exporter {

    /**
     * Map<ServiceName, IProcessor>
     */
    private static ConcurrentMap<String, IProcessor> services = new ConcurrentHashMap<>();

    /**
     * Add service
     *
     * @param serviceId service id
     * @param processor service processor
     */
    public static void addService(String serviceId, IProcessor processor) {
        if (services.containsKey(serviceId)) {
            throw new MioFrameException("The service already exist.");
        }
        services.put(serviceId, processor);
    }

    /**
     * Export server
     *
     * @return successful return to true
     */
    public static boolean export() {
        return true;
    }

    /**
     * Destroy server
     */
    public static void destroy() {
        services.clear();
    }

}
