package io.mio.commons.metric;

import io.mio.commons.extension.SPI;

import java.util.Map;

@SPI(single = true)
public interface Metric {

    /**
     * 收集指标
     *
     * @return
     */
    Map<String, Object> collectMetrics();

}
