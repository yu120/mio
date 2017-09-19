package io.mio;

import io.mio.transport.MioServer;
import io.mio.transport.protocol.FixedLengthProtocol;

import java.io.IOException;

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
