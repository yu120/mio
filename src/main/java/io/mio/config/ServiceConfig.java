package io.mio.config;

import io.mio.commons.URL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Service Config
 *
 * @author lry
 */
@Data
@ToString
@EqualsAndHashCode
public class ServiceConfig implements Serializable {

    /**
     * Service name
     */
    protected String name;

    /**
     * Service group
     */
    protected String group;

    /**
     * Service version
     */
    protected String version = "1.0.0";

    /**
     * Service modules, e.g: method names
     */
    protected List<String> modules;

    /**
     * whether the service is deprecated
     */
    protected Boolean deprecated = false;

    /**
     * The service weight
     */
    protected Integer weight = 100;

    /**
     * Document center
     */
    protected String document;

    /**
     * The application config
     */
    private ApplicationConfig application;

    /**
     * The protocol config
     */
    private ProtocolConfig protocol;

    /**
     * The registry center config
     */
    private RegistryConfig registry;

    public URL buildURL() {
        URL url = new URL(protocol.getName(), protocol.getHost(), protocol.getPort(), name);
        return url;
    }

    public static ServiceConfig parseURL(URL url) {
        return null;
    }

}
