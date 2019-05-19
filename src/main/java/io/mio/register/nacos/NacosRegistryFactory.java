package io.mio.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.naming.utils.StringUtils;
import io.mio.commons.URL;
import io.mio.commons.extension.Extension;
import io.mio.register.Registry;
import io.mio.register.Constants;
import io.mio.register.support.AbstractRegistryFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;

@Slf4j
@Extension("fastjson")
public class NacosRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected Registry createRegistry(URL url) {
        Properties properties = new Properties();
        StringBuilder sb = new StringBuilder(url.getHost()).append(":").append(url.getPort());
        String backup = url.getParameter(Constants.BACKUP_KEY);
        if (backup != null) {
            sb.append(",").append(backup);
        }

        properties.put(PropertyKeyConst.SERVER_ADDR, sb.toString());
        if (StringUtils.isNotEmpty(url.getParameter(PropertyKeyConst.NAMESPACE))) {
            properties.setProperty(PropertyKeyConst.NAMESPACE, url.getParameter(PropertyKeyConst.NAMESPACE));
        }
        if (StringUtils.isNotEmpty(url.getParameter(UtilAndComs.NACOS_NAMING_LOG_NAME))) {
            properties.setProperty(UtilAndComs.NACOS_NAMING_LOG_NAME, url.getParameter(UtilAndComs.NACOS_NAMING_LOG_NAME));
        }
        if (StringUtils.isNotEmpty(url.getParameter(PropertyKeyConst.ENDPOINT))) {
            properties.setProperty(PropertyKeyConst.ENDPOINT, url.getParameter(PropertyKeyConst.ENDPOINT));
        }
        if (StringUtils.isNotEmpty(url.getParameter(PropertyKeyConst.ACCESS_KEY))) {
            properties.setProperty(PropertyKeyConst.ACCESS_KEY, url.getParameter(PropertyKeyConst.ACCESS_KEY));
        }
        if (StringUtils.isNotEmpty(url.getParameter(PropertyKeyConst.SECRET_KEY))) {
            properties.setProperty(PropertyKeyConst.SECRET_KEY, url.getParameter(PropertyKeyConst.SECRET_KEY));
        }
        if (StringUtils.isNotEmpty(url.getParameter(PropertyKeyConst.CLUSTER_NAME))) {
            properties.setProperty(PropertyKeyConst.CLUSTER_NAME, url.getParameter(PropertyKeyConst.CLUSTER_NAME));
        }

        try {
            return new NacosRegistry(url, NacosFactory.createNamingService(properties));
        } catch (NacosException e) {
            log.error(e.getErrMsg(), e);
            throw new IllegalStateException(e);
        }
    }

}
