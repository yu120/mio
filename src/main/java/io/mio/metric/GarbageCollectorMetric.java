package io.mio.metric;

import io.mio.commons.extension.Extension;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 垃圾回收的指标收集器
 *
 * @author lry
 */
@Extension("gc")
public class GarbageCollectorMetric implements Metric {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private final List<GarbageCollectorMXBean> garbageCollectors;
    private static Map<String, Object> lastMetric = new LinkedHashMap<>();

    public GarbageCollectorMetric() {
        this.garbageCollectors = new ArrayList<>(ManagementFactory.getGarbageCollectorMXBeans());
    }

    @Override
    public Map<String, Object> collectMetrics() {
        final Map<String, Object> gauges = new HashMap<>();
        for (final String metricsKey : getDatas().keySet()) {
            gauges.put(metricsKey, getDatas().get(metricsKey));
        }

        return Collections.unmodifiableMap(gauges);
    }

    /**
     * ps_scavenge.count:新生代PS（并行扫描）次数
     * ps_scavenge.time:单位：秒,新生代PS（并行扫描）时间
     * ps_marksweep.count:老年代CMS（并行标记清扫）次数
     * ps_marksweep_time:单位：秒,老年代CMS（并行标记清扫）时间
     * <p>
     * ps_scavenge_diff_count:新生代PS（并行扫描）变化次数
     * ps_scavenge_diff_time: 单位：秒,新生代PS（并行扫描）变化时间
     * ps_marksweep_diff_count: 老年代CMS（并行标记清扫）变化次数
     * ps_marksweep_diff_time: 单位：秒,老年代CMS（并行标记清扫）变化时间
     *
     * @return
     */
    public Map<String, Double> getDatas() {
        final Map<String, Double> gauges = new LinkedHashMap<>();
        for (final GarbageCollectorMXBean gc : garbageCollectors) {
            final String name = "gc_" + WHITESPACE.matcher(gc.getName()).replaceAll("_").toLowerCase();

            String lastCountKey = name + "_diff_count";
            Object lastCountValue = lastMetric.get(lastCountKey);
            lastCountValue = (lastCountValue == null) ? 0 : lastCountValue;
            long lastCountCurrent = gc.getCollectionCount();
            long lastCountKV = lastCountCurrent - Long.valueOf(lastCountValue + "");
            lastMetric.put(lastCountKey, lastCountCurrent);
            gauges.put(lastCountKey, (double) lastCountKV);

            String lastTimeKey = name + "_diff_time";
            Object lastTimeVal = lastMetric.get(lastTimeKey);
            lastTimeVal = (lastTimeVal == null) ? 0 : lastTimeVal;
            double lastTimeCurrent = (double) gc.getCollectionTime();
            double lastTimeKV = lastTimeCurrent - Double.valueOf(lastTimeVal + "");
            lastMetric.put(lastTimeKey, lastTimeCurrent);
            gauges.put(lastTimeKey, Double.valueOf(String.format("%.3f", lastTimeKV / 1000)));
            gauges.put(name + "_count", (double) lastCountCurrent);
            // 单位：从毫秒转换为秒
            gauges.put(name + "_time", Double.valueOf(String.format("%.3f", lastTimeCurrent / 1000)));
        }

        return gauges;
    }

}
