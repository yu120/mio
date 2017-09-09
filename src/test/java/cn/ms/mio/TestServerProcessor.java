package cn.ms.mio;

import java.io.IOException;

import cn.ms.mio.transport.MioSession;
import cn.ms.mio.transport.processor.MessageProcessor;

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
