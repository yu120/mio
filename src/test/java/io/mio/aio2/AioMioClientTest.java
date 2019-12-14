package io.mio.aio2;

import io.mio.ClientConfig;
import io.mio.MioCallback;
import io.mio.MioMessage;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AioMioClientTest {

    public static void main(String[] args) throws Throwable {
        AioMioClient aioMioClient = new AioMioClient();
        aioMioClient.initialize(new ClientConfig());
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 9999);
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("msg", "头部参数" + i);
            MioMessage mioMessage = MioMessage.build(headers, (i + "-hello").getBytes(StandardCharsets.UTF_8));
            mioMessage.setRemoteAddress(socketAddress);
            aioMioClient.callback(mioMessage, new MioCallback<MioMessage>() {
                @Override
                public void onSuccess(MioMessage result) {
                    System.out.println("客户端收到响应:" + result);
                }
            });

        }

        Thread.sleep(10000);
    }

}
