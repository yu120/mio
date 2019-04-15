package io.mio.register;

import io.mio.commons.URL;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.retry.ExponentialBackoffRetry;

@Getter
@Slf4j
public class ZookeeperRegistry implements IRegistry {

    private static final String SESSION_KEY = "session";
    private static final String TIMEOUT_KEY = "timeout";

    private CuratorFramework curatorFramework;

    public static void main(String[] args) throws Exception {
        URL url = URL.valueOf("zookeeper://127.0.0.1:2181");
        ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry();
        zookeeperRegistry.initialize(url);
        zookeeperRegistry.register(URL.valueOf("mio://127.0.0.1:8080/cn.test.DemoService"));
        Thread.sleep(1000 * 100);
    }

    @Override
    public void initialize(URL url) {
        try {
            this.curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString(url.buildServerAddress())
                    .sessionTimeoutMs(url.getParameter(SESSION_KEY, 5000))
                    .connectionTimeoutMs(url.getParameter(TIMEOUT_KEY, 5000))
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
            curatorFramework.start();
            curatorFramework.blockUntilConnected();
        } catch (Exception e) {
            log.error("The connection zookeeper is fail", e);
        }
    }

    @Override
    public void register(URL url) {
        String serviceName = url.getServiceName();
        CreateBuilder createBuilder = curatorFramework.create();

        try {
            String returnPath = createBuilder.creatingParentsIfNeeded().forPath(serviceName);
        } catch (Exception e) {

        }
    }

    @Override
    public void destroy() {
        if (curatorFramework != null) {
            curatorFramework.close();
        }
    }

}
