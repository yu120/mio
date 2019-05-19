package io.mio.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.naming.utils.StringUtils;
import io.mio.commons.URL;
import io.mio.register.Registry;
import io.mio.register.Constants;
import io.mio.register.support.AbstractRegistryFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;

@Slf4j
public class NacosRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected Registry createRegistry(URL url) {
        Properties properties = new Properties();
        StringBuilder sb = new StringBuilder(url.getHost()).append(":").append(url.getPort());
        String backup = url.getParameter(Constants.BACKUP_KEY);
        if (backup != null) {
            sb.append(",").append(backup);
        }

        properties.put(SERVER_ADDR, sb.toString());
        if (StringUtils.isNotEmpty(url.getParameter(NAMESPACE))) {
            properties.setProperty(NAMESPACE, url.getParameter(NAMESPACE));
        }
        if (StringUtils.isNotEmpty(url.getParameter(NACOS_NAMING_LOG_NAME))) {
            properties.setProperty(NACOS_NAMING_LOG_NAME, url.getParameter(NACOS_NAMING_LOG_NAME));
        }
        if (StringUtils.isNotEmpty(url.getParameter(ENDPOINT))) {
            properties.setProperty(ENDPOINT, url.getParameter(ENDPOINT));
        }
        if (StringUtils.isNotEmpty(url.getParameter(ACCESS_KEY))) {
            properties.setProperty(ACCESS_KEY, url.getParameter(ACCESS_KEY));
        }
        if (StringUtils.isNotEmpty(url.getParameter(SECRET_KEY))) {
            properties.setProperty(SECRET_KEY, url.getParameter(SECRET_KEY));
        }
        if (StringUtils.isNotEmpty(url.getParameter(CLUSTER_NAME))) {
            properties.setProperty(CLUSTER_NAME, url.getParameter(CLUSTER_NAME));
        }

        try {
            return new NacosRegistry(url, NacosFactory.createNamingService(properties));
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new IllegalStateException(e);
        }
    }

}
