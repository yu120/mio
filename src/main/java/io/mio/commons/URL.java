package io.mio.commons;

import io.mio.exception.MioFrameException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uniform Resource Location
 *
 * @author lry
 */
@Data
public class URL {

    public static final String BACKUP_KEY = "backup";
    public static final String VERSION_KEY = "version";
    public static final String GROUP_KEY = "group";
    public static final String APPLICATION_KEY = "application";

    private String protocol;
    private String host;
    private int port;
    private String path;
    private Map<String, String> parameters;
    private volatile transient Map<String, Number> numbers;

    public URL(String protocol, String host, int port, String path) {
        this(protocol, host, port, path, new HashMap<>());
    }

    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.parameters = parameters;
    }

    public static URL valueOf(String url) {
        if (StringUtils.isBlank(url)) {
            throw new MioFrameException("url is null");
        }
        String protocol = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = new HashMap<>();
        int i = url.indexOf("?");
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if (i == 0) {
                throw new IllegalStateException("url missing protocol: " + url);
            }
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        } else {
            i = url.indexOf(":/");
            if (i >= 0) {
                if (i == 0) {
                    throw new IllegalStateException("url missing protocol: " + url);
                }
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }

        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }

        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) {
            host = url;
        }

        return new URL(protocol, host, port, path, parameters);
    }

    private static String buildHostPortStr(String host, int defaultPort) {
        if (defaultPort <= 0) {
            return host;
        }

        int idx = host.indexOf(":");
        if (idx < 0) {
            return host + ":" + defaultPort;
        }

        int port = Integer.parseInt(host.substring(idx + 1));
        if (port <= 0) {
            return host.substring(0, idx + 1) + defaultPort;
        }
        return host;
    }

    public URL createCopy() {
        Map<String, String> params = new HashMap<>();
        if (this.parameters != null) {
            params.putAll(this.parameters);
        }

        return new URL(protocol, host, port, path, params);
    }

    public String getVersion() {
        return getParameter(URLParamType.VERSION.getName(), URLParamType.VERSION.getValue());
    }

    public String getGroup() {
        return getParameter(URLParamType.GROUP.getName(), URLParamType.GROUP.getValue());
    }

    public String getApplication() {
        return getParameter(URLParamType.APPLICATION.getName(), URLParamType.APPLICATION.getValue());
    }

    public double getWeight() {
        return getParameter(URLParamType.WEIGHT.getName(), URLParamType.WEIGHT.getDoubleValue());
    }

    public String getCluster() {
        return getParameter(URLParamType.CLUSTER.getName(), URLParamType.CLUSTER.getValue());
    }

    public boolean getHealthy() {
        return getParameter(URLParamType.HEALTHY.getName(), URLParamType.HEALTHY.isBoolValue());
    }

    public boolean getEnabled() {
        return getParameter(URLParamType.ENABLED.getName(), URLParamType.ENABLED.isBoolValue());
    }

    public String buildServerAddr() {
        StringBuilder serverAddrBuilder = new StringBuilder(host).append(":").append(port);
        String backup = this.getParameter(URL.BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        return serverAddrBuilder.toString();
    }

    public void addParameters(Map<String, String> params) {
        parameters.putAll(params);
    }

    public void addParameter(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        parameters.put(name, value);
    }

    public void addParameterIfAbsent(String name, String value) {
        if (hasParameter(name)) {
            return;
        }
        parameters.put(name, value);
    }

    public void removeParameter(String name) {
        if (name != null) {
            parameters.remove(name);
        }
    }

    // ==== get parameter start

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String getParameter(String name, String defaultValue) {
        String value = getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public Boolean getParameter(String name, boolean defaultValue) {
        String value = getParameter(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    public Integer getParameter(String name, int defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.intValue();
        }
        String value = parameters.get(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(name, i);
        return i;
    }

    public Long getParameter(String name, long defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.longValue();
        }
        String value = parameters.get(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(name, l);
        return l;
    }

    public Double getParameter(String name, double defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.doubleValue();
        }
        String value = parameters.get(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        double l = Double.parseDouble(value);
        getNumbers().put(name, l);
        return l;
    }

    public Float getParameter(String name, float defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.floatValue();
        }
        String value = parameters.get(name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(name, f);
        return f;
    }

    // ==== get parameter end

    // ==== get method parameter start

    public String getMethodParameter(String methodName, String paramDesc, String name) {
        String value = getParameter("method." + methodName + "(" + paramDesc + ")." + name);
        if (value == null || value.length() == 0) {
            return getParameter(name);
        }
        return value;
    }

    public String getMethodParameter(String methodName, String paramDesc, String name, String defaultValue) {
        String value = getMethodParameter(methodName, paramDesc, name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public Boolean getMethodParameter(String methodName, String paramDesc, String name, boolean defaultValue) {
        String value = getMethodParameter(methodName, paramDesc, name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public Integer getMethodParameter(String methodName, String paramDesc, String name, int defaultValue) {
        String key = methodName + "(" + paramDesc + ")." + name;
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(methodName, paramDesc, name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    public Long getMethodParameter(String methodName, String paramDesc, String name, long defaultValue) {
        String key = methodName + "(" + paramDesc + ")." + name;
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getMethodParameter(methodName, paramDesc, name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    public Float getMethodParameter(String methodName, String paramDesc, String name, float defaultValue) {
        String key = methodName + "(" + paramDesc + ")." + name;
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.floatValue();
        }
        String value = getMethodParameter(methodName, paramDesc, name);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        float f = Float.parseFloat(value);
        getNumbers().put(key, f);
        return f;
    }

    // ==== get method parameter end

    public String getUri() {
        return protocol + "://" + host + ":" + port + "/" + path;
    }

    public String getIdentity() {
        return protocol + "://" + host + ":" + port +
                "/" + getParameter(URLParamType.CLUSTER.getName(), URLParamType.CLUSTER.getValue()) +
                "/" + getParameter(URLParamType.GROUP.getName(), URLParamType.GROUP.getValue()) +
                "/" + getPath() +
                "/" + getParameter(URLParamType.VERSION.getName(), URLParamType.VERSION.getValue());
    }

    public String getServiceName() {
        return getParameter(URLParamType.CLUSTER.getName(), URLParamType.CLUSTER.getValue()) +
                "/" + getParameter(URLParamType.GROUP.getName(), URLParamType.GROUP.getValue()) +
                "/" + getPath() +
                "/" + getParameter(URLParamType.VERSION.getName(), URLParamType.VERSION.getValue());
    }

    public String toFullStr() {
        StringBuilder builder = new StringBuilder();
        builder.append(getUri()).append("?");
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        return builder.toString();
    }

    public boolean hasParameter(String key) {
        return StringUtils.isNotBlank(getParameter(key));
    }

    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<>();
        }

        return numbers;
    }

    @Override
    public int hashCode() {
        int factor = 31;
        int rs = 1;
        rs = factor * rs + Objects.hashCode(protocol);
        rs = factor * rs + Objects.hashCode(host);
        rs = factor * rs + Objects.hashCode(port);
        rs = factor * rs + Objects.hashCode(path);
        rs = factor * rs + Objects.hashCode(parameters);
        return rs;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof URL)) {
            return false;
        }
        URL ou = (URL) obj;
        if (!Objects.equals(this.protocol, ou.protocol)) {
            return false;
        }
        if (!Objects.equals(this.host, ou.host)) {
            return false;
        }
        if (!Objects.equals(this.port, ou.port)) {
            return false;
        }
        if (!Objects.equals(this.path, ou.path)) {
            return false;
        }

        return Objects.equals(this.parameters, ou.parameters);
    }

    @Override
    public String toString() {
        return getUri() + "?group=" + getGroup();
    }

}