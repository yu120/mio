package io.mio.transport.netty4;

import io.mio.core.transport.ClientConfig;
import io.mio.core.commons.MioMessage;
import io.mio.core.transport.MioClient;
import io.mio.core.transport.MioTransport;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NettyMioClientTest {

    public static void main(String[] args) throws Throwable {
        ClientConfig clientConfig = new ClientConfig();
        MioClient mioClient = MioTransport.createClient(clientConfig);
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9999);
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("msg", "头部参数" + i);
            MioMessage mioMessage = new MioMessage((i + "-hello").getBytes(StandardCharsets.UTF_8), headers);
            mioMessage.setRemoteAddress(socketAddress);
            MioMessage response = mioClient.request(mioMessage);
            System.out.println("客户端收到响应:" + response);
        }

        Thread.sleep(1000);
    }

}
