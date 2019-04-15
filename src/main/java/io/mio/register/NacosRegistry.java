package io.mio.register;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import io.mio.commons.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class NacosRegistry {

    public static void main(String[] args) {
        URL url = URL.valueOf("nacos://127.0.0.1:8848");
        NamingService namingService = new NacosRegistry().buildNamingService(url);
        System.out.println(namingService.getServerStatus());
    }

    private NamingService buildNamingService(URL url) {
        Properties properties = this.buildProperties(url);
        NamingService namingService;
        try {
            namingService = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new IllegalStateException(e);
        }

        return namingService;
    }

    private Properties buildProperties(URL url) {
        Properties properties = new Properties();
        StringBuilder serverAddrBuilder = new StringBuilder(url.getHost()).append(":").append(url.getPort());
        String backup = url.getParameter(URL.BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddrBuilder.toString());
        if (url.getParameters().containsKey(PropertyKeyConst.NAMESPACE)) {
            properties.setProperty(PropertyKeyConst.NAMESPACE, url.getParameter(PropertyKeyConst.NAMESPACE));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.ENDPOINT)) {
            properties.setProperty(PropertyKeyConst.ENDPOINT, url.getParameter(PropertyKeyConst.ENDPOINT));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.ACCESS_KEY)) {
            properties.setProperty(PropertyKeyConst.ACCESS_KEY, url.getParameter(PropertyKeyConst.ACCESS_KEY));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.SECRET_KEY)) {
            properties.setProperty(PropertyKeyConst.SECRET_KEY, url.getParameter(PropertyKeyConst.SECRET_KEY));
        }
        if (url.getParameters().containsKey(PropertyKeyConst.CLUSTER_NAME)) {
            properties.setProperty(PropertyKeyConst.CLUSTER_NAME, url.getParameter(PropertyKeyConst.CLUSTER_NAME));
        }

        return properties;
    }

}
