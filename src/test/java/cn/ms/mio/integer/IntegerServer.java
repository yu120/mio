package cn.ms.mio.integer;

import java.io.IOException;

import cn.ms.mio.transport.MioServer;

public class IntegerServer {
    public static void main(String[] args) {
        MioServer<Integer> server = new MioServer<Integer>()
                .bind(8888)
                .setProtocol(new IntegerProtocol())
                .setProcessor(new IntegerServerProcessor());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
