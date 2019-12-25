package io.mio.netty;

import io.mio.MioServer;
import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import io.mio.commons.ServerConfig;
import io.mio.commons.extension.ExtensionLoader;

import java.util.function.Consumer;

public class NettyMioServerTest {

    public static void main(String[] args) throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCodec("http");
        MioServer mioServer = ExtensionLoader.getLoader(MioServer.class).getExtension();
        mioServer.initialize(serverConfig, new MioCallback<MioMessage>() {
            @Override
            public void onProcessor(Consumer<MioMessage> consumer, MioMessage result) {
                System.out.println("服务端收到：" + result);
                // 当服务端完成写操作后，关闭与客户端的连接
                MioMessage mioMessage = new MioMessage(null, "你好".getBytes());
                consumer.accept(mioMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }
        });
    }

}
