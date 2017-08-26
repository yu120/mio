package cn.ms.mio;

import cn.ms.mio.service.process.MessageProcessor;
import cn.ms.mio.transport.MioSession;

/**
 * @author Seer
 * @version V1.0 , 2017/8/23
 */
public class IntegerClientProcessor implements MessageProcessor<Integer> {
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
