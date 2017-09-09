package cn.ms.mio;

import java.io.IOException;

import cn.ms.mio.transport.MioServer;
import cn.ms.mio.transport.protocol.FixedLengthProtocol;

public class TestServer {
    public static void main(String[] args) {
        MioServer<byte[]> server = new MioServer<byte[]>()
                .bind(8888)
                .setProtocol(new FixedLengthProtocol<byte[]>())
                .setProcessor(new TestServerProcessor());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
