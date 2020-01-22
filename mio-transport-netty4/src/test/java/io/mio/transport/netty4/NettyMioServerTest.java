package io.mio.transport.netty4;

import io.mio.core.commons.MioCallback;
import io.mio.core.commons.MioMessage;
import io.mio.core.transport.ServerConfig;
import io.mio.core.transport.MioServer;
import io.mio.core.transport.MioTransport;

import java.util.function.Consumer;

public class NettyMioServerTest {

    public static void main(String[] args) throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        MioServer mioServer = MioTransport.createServer(serverConfig, new MioCallback<MioMessage>() {
            @Override
            public void onProcessor(Consumer<MioMessage> context, MioMessage request) {
                System.out.println("服务端收到：" + request);
                // 当服务端完成写操作后，关闭与客户端的连接
                MioMessage mioMessage = new MioMessage(null, "你好".getBytes());
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
