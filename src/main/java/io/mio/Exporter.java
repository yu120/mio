package io.mio;

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
     * 暴露一个服务点
     *
     * @return
     */
    public static boolean export() {
        return true;
    }

}
