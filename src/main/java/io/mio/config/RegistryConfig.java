package io.mio.config;

import io.mio.annotation.MioRegistry;
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
     * Protocol for register center
     */
    private String protocol;

    /**
     * Register center host
     */
    private String host;

    /**
     * Default port for register center
     */
    private int port;

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
     * Request timeout in milliseconds for register center
     */
    private long timeout = 10000L;

    /**
     * Session timeout in milliseconds for register center
     */
    private long session = 5000L;

    /**
     * The customized parameters
     */
    private Map<String, Object> parameters = new HashMap<>();

    public static RegistryConfig build(MioRegistry mioRegistry) {
        if (mioRegistry == null) {
            return null;
        }

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol(mioRegistry.protocol());
        registryConfig.setHost(mioRegistry.host());
        registryConfig.setPort(mioRegistry.port());
        return registryConfig;
    }

}
