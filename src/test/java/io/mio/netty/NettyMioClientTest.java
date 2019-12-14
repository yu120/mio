package io.mio.netty;

import io.mio.commons.ClientConfig;
import io.mio.commons.MioMessage;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NettyMioClientTest {

    public static void main(String[] args) throws Throwable {
        NettyMioClient nettyMioClient = new NettyMioClient();
        nettyMioClient.initialize(new ClientConfig());
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9999);
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("msg", "头部参数" + i);
            MioMessage mioMessage = MioMessage.build(headers, (i + "-hello").getBytes(StandardCharsets.UTF_8));
            mioMessage.setRemoteAddress(socketAddress);
            MioMessage response = nettyMioClient.request(mioMessage);
            System.out.println("客户端收到响应:" + response);
        }

        Thread.sleep(10000);
    }

}
