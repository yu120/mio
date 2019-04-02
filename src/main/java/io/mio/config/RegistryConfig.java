package io.mio.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry Config
 *
 * @author lry
 */
@Data
@ToString
@EqualsAndHashCode
public class RegistryConfig implements Serializable {

    /**
     * Register center address
     */
    private String address;

    /**
     * Protocol for register center
     */
    private String protocol;

    /**
     * Default port for register center
     */
    private Integer port;

    /**
     * Username to login register center
     */
    private String username;

    /**
     * Password to login register center
     */
    private String password;

    /**
     * The group the services registry in
     */
    private String group;

    /**
     * The version the services registry in
     */
    private String version = "1.0.0";

    /**
     * Request timeout in milliseconds for register center
     */
    private Long timeout = 10000L;

    /**
     * Session timeout in milliseconds for register center
     */
    private Long session = 5000L;

    /**
     * The customized parameters
     */
    private Map<String, String> parameters = new HashMap<>();

}
