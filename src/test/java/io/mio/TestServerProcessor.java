package io.mio;

import io.mio.transport.MioSession;
import io.mio.transport.processor.MessageProcessor;

import java.io.IOException;

public class TestServerProcessor implements MessageProcessor<byte[]> {
    @Override
    public void process(MioSession<byte[]> session, byte[] msg) {
        System.out.println("接受到客户端数据：" + msg + " ,响应数据:" + msg);
        try {
            session.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initSession(MioSession<byte[]> session) {

    }
}
