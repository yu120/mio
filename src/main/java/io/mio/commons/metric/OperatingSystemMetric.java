package io.mio.commons.metric;

import io.mio.commons.extension.Extension;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统平均负载(满负荷状态为1.00*CPU核数)
 *
 * @author lry
 */
@Extension("os")
public class OperatingSystemMetric implements Metric {

    @Override
    public Map<String, Object> collectMetrics() {
        final Map<String, Object> gauges = new HashMap<>();
        gauges.put("os_load_average", getData());

        return Collections.unmodifiableMap(gauges);
    }

    /**
     * 系统平均负载(满负荷状态为1.00*CPU核数)
     *
     * @return
     */
    public Double getData() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        double load;
        try {
            Method method = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage");
            load = (Double) method.invoke(operatingSystemMXBean, new Object[0]);
        } catch (Throwable e) {
            load = -1;
        }

        int cpu = operatingSystemMXBean.getAvailableProcessors();
        return Double.valueOf(String.format("%.4f", load / cpu));
    }

}
