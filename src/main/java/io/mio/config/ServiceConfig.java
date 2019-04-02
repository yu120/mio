package io.mio.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

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
     * The service name
     */
    protected String name;

    /**
     * The service version
     */
    protected String version;

    /**
     * The service group
     */
    protected String group;

    /**
     * whether the service is deprecated
     */
    protected Boolean deprecated = false;

    /**
     * The service weight
     */
    protected Integer weight;

    /**
     * Document center
     */
    protected String document;

    /**
     * The providerIds
     */
    private String providerIds;

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

}
