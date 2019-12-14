package io.mio.netty;

import io.mio.MioCallback;
import io.mio.MioMessage;
import io.mio.ServerConfig;

import java.util.function.Consumer;

public class NettyMioServerTest {

    public static void main(String[] args) throws Exception {
        NettyMioServer nettyMioServer = new NettyMioServer();
        nettyMioServer.initialize(new ServerConfig(), new MioCallback<MioMessage>() {
            @Override
            public void onProcessor(Consumer<MioMessage> consumer, MioMessage result) {
                System.out.println("服务端收到：" + result);
                // 当服务端完成写操作后，关闭与客户端的连接
                MioMessage mioMessage = MioMessage.build(result.getHeaders(), result.getData());
                consumer.accept(mioMessage);
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }
        });
    }

}
