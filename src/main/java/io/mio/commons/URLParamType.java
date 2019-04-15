package io.mio.commons;

import lombok.Getter;

import java.lang.management.ManagementFactory;

@Getter
public enum URLParamType {

    // ===

    CLUSTER("cluster", "default-cluster"),
    PROTOCOL("protocol", "mio"),
    GROUP("group", "default_group"),
    VERSION("version", "1.0.0"),
    APPLICATION("application", ""),
    WEIGHT("weight", 1.0d),
    HEALTHY("healthy", true),
    ENABLED("enabled", true),
    CPU_NUM("cpuNum", Runtime.getRuntime().availableProcessors()),
    PID("pid", ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

    private String name;
    private String value;
    private long longValue;
    private int intValue;
    private boolean boolValue;
    private double doubleValue;

    URLParamType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    URLParamType(String name, long longValue) {
        this.name = name;
        this.value = String.valueOf(longValue);
        this.longValue = longValue;
    }

    URLParamType(String name, int intValue) {
        this.name = name;
        this.value = String.valueOf(intValue);
        this.intValue = intValue;
    }

    URLParamType(String name, boolean boolValue) {
        this.name = name;
        this.value = String.valueOf(boolValue);
        this.boolValue = boolValue;
    }

    URLParamType(String name, double doubleValue) {
        this.name = name;
        this.value = String.valueOf(doubleValue);
        this.doubleValue = doubleValue;
    }

}
