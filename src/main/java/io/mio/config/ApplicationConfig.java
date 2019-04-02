package io.mio.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Application Config
 *
 * @author lry
 */
@Data
@ToString
@EqualsAndHashCode
public class ApplicationConfig implements Serializable {

    /**
     * Application name
     */
    private String name;

    /**
     * Application version
     */
    private String version = "1.0.0";

    /**
     * Application owner
     */
    private String owner = "lmx";

    /**
     * Application's organization
     */
    private String organization = "micro";

    /**
     * Architecture layer, e.g: pub-srv、biz-srv、gw-srv
     */
    private String layer = "biz-srv";

    /**
     * Environment, e.g: dev,test,prod
     */
    private String environment = "dev";

    /**
     * Customized parameters
     */
    private Map<String, String> parameters = new HashMap<>();

}
