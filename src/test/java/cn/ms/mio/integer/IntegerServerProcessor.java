package cn.ms.mio.integer;

import cn.ms.mio.transport.support.IProcessor;
import cn.ms.mio.transport.support.MioSession;

public class IntegerServerProcessor implements IProcessor<Integer> {
    @Override
    public void process(MioSession<Integer> session, Integer msg) throws Exception {
        Integer respMsg=msg+1;
        System.out.println("接受到客户端数据：" + msg + " ,响应数据:" + (respMsg));
        session.write(respMsg);
    }

    @Override
    public void initSession(MioSession<Integer> session) {

    }
}
