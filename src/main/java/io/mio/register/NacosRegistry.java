package io.mio.register;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.mio.commons.URL;
import io.mio.commons.URLParamType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Properties;

@Getter
@Slf4j
public class NacosRegistry implements IRegistry {

    private NamingService namingService;

    public static void main(String[] args) throws Exception {
        URL url = URL.valueOf("nacos://127.0.0.1:8848");
        NacosRegistry nacosRegistry = new NacosRegistry();
        nacosRegistry.initialize(url);
        System.out.println(nacosRegistry.getNamingService().getServerStatus());
        nacosRegistry.register(URL.valueOf("mio://127.0.0.1:8080/cn.test.DemoService"));
        Thread.sleep(1000 * 100);
    }

    @Override
    public void initialize(URL url) {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, url.buildServerAddress());
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

        try {
            this.namingService = NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void register(URL url) {
        String serviceName = url.getServiceName();
        Instance instance = new Instance();
        instance.setInstanceId(url.getHost() + "@" + URLParamType.PID.getValue());
        instance.setIp(url.getHost());
        instance.setPort(url.getPort());
        instance.setWeight(url.getWeight());
        instance.setHealthy(url.getHealthy());
        instance.setEnabled(url.getEnabled());
        instance.setClusterName(url.getCluster());
        instance.setServiceName(serviceName);
        instance.setMetadata(new HashMap<>(url.getParameters()));

        try {
            namingService.registerInstance(serviceName, instance);
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {

    }

}
