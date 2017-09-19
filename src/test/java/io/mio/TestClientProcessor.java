package io.mio;

import io.mio.transport.MioSession;
import io.mio.transport.processor.MessageProcessor;

public class TestClientProcessor implements MessageProcessor<byte[]> {
    private MioSession<byte[]> session;

    @Override
    public void process(MioSession<byte[]> session, byte[] msg) {
        System.out.println("接受到服务端响应数据：" + msg);
    }

    @Override
    public void initSession(MioSession<byte[]> session) {
        this.session = session;
    }

    public MioSession<byte[]> getSession() {
        return session;
    }
}
