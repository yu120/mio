package cn.ms.mio.integer;

import cn.ms.mio.service.process.IProcessor;
import cn.ms.mio.transport.support.MioSession;

public class IntegerClientProcessor implements IProcessor<Integer> {
    private MioSession<Integer> session;

    @Override
    public void process(MioSession<Integer> session, Integer msg) throws Exception {
        System.out.println("接受到服务端响应数据：" + msg);
    }

    @Override
    public void initSession(MioSession<Integer> session) {
        this.session = session;
    }

    public MioSession<Integer> getSession() {
        return session;
    }
}
