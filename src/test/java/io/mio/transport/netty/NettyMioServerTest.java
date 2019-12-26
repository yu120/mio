package io.mio.transport.netty;

import io.mio.transport.MioServer;
import io.mio.transport.MioTransport;
import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;

import java.util.function.Consumer;

public class NettyMioServerTest {

    public static void main(String[] args) throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        MioServer mioServer = MioTransport.createServer(serverConfig, new MioCallback<MioMessage>() {
            @Override
            public void onProcessor(Consumer<MioMessage> context, MioMessage request) {
                System.out.println("服务端收到：" + request);
                // 当服务端完成写操作后，关闭与客户端的连接
                MioMessage mioMessage = new MioMessage(null, "".getBytes(), "你好".getBytes());
                context.accept(mioMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }
        });
        while (true) {
            Thread.sleep(10000);
        }
    }

}
