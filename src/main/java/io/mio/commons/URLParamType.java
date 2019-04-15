package io.mio.commons;

import lombok.Getter;

@Getter
public enum URLParamType {

    // ===

    VERSION("version", "1.0.0"),
    GROUP("group", "default_rpc"),
    APPLICATION("application", ""),
    CPU_NUM("cpuNum", Runtime.getRuntime().availableProcessors());

    private String name;
    private String value;
    private long longValue;
    private int intValue;
    private boolean boolValue;

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

}
