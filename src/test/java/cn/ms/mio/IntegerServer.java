package cn.ms.mio;

import cn.ms.mio.transport.MioServer;

import java.io.IOException;

/**
 * Created by seer on 2017/7/12.
 */
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
