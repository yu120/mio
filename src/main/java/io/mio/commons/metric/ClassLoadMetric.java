package io.mio.commons.metric;

import io.mio.commons.extension.Extension;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 虚拟机的类加载系统负载值<br>
 * <br>
 * A set of gauges for JVM classloader usage.<br>
 *
 * @author lry
 */
@Extension("loaded")
public class ClassLoadMetric implements Metric {

    private final ClassLoadingMXBean mxBean;

    public ClassLoadMetric() {
        this.mxBean = ManagementFactory.getClassLoadingMXBean();
    }

    @Override
    public Map<String, Object> collectMetrics() {
        final Map<String, Object> gauges = new HashMap<>();
        gauges.put("loaded_totalLoadedCount", mxBean.getTotalLoadedClassCount());
        gauges.put("loaded_unloadedCount", mxBean.getUnloadedClassCount());
        gauges.put("loaded_loadedCount", mxBean.getLoadedClassCount());

        return Collections.unmodifiableMap(gauges);
    }

}
