/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mio.register.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.utils.StringUtils;
import io.mio.commons.URL;
import io.mio.register.Registry;
import io.mio.register.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.client.naming.utils.UtilAndComs.NACOS_NAMING_LOG_NAME;

public class NacosRegistryFactory  {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Registry createRegistry(URL url) {
        return new NacosRegistry(url, buildNamingService(url));
    }

    private NamingService buildNamingService(URL url) {
        Properties nacosProperties = buildNacosProperties(url);
        NamingService namingService;
        try {
            namingService = NacosFactory.createNamingService(nacosProperties);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
            throw new IllegalStateException(e);
        }
        return namingService;
    }

    private Properties buildNacosProperties(URL url) {
        Properties properties = new Properties();
        setServerAddr(url, properties);
        setProperties(url, properties);
        return properties;
    }

    private void setServerAddr(URL url, Properties properties) {
        StringBuilder serverAddrBuilder =
                new StringBuilder(url.getHost())
                        .append(":")
                        .append(url.getPort());

        // Append backup parameter as other servers
        String backup = url.getParameter(Constants.BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        String serverAddr = serverAddrBuilder.toString();
        properties.put(SERVER_ADDR, serverAddr);
    }

    private void setProperties(URL url, Properties properties) {
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        putPropertyIfAbsent(url, properties, ENDPOINT);
        putPropertyIfAbsent(url, properties, ACCESS_KEY);
        putPropertyIfAbsent(url, properties, SECRET_KEY);
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);
    }

    private void putPropertyIfAbsent(URL url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }
}
