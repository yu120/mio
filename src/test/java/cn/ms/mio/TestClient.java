package cn.ms.mio;

import cn.ms.mio.transport.MioClient;
import cn.ms.mio.transport.protocol.FixedLengthProtocol;

public class TestClient {
    public static void main(String[] args) throws Exception {
        TestClientProcessor processor=new TestClientProcessor();
        MioClient<byte[]> aioQuickClient=new MioClient<byte[]>()
                .connect("localhost",8888)
                .setProtocol(new FixedLengthProtocol<byte[]>())
                .setProcessor(processor);
        aioQuickClient.start();
        processor.getSession().write("测试".getBytes());
        Thread.sleep(1000);
        aioQuickClient.shutdown();

    }
}
