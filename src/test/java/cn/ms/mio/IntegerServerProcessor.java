package cn.ms.mio;

import cn.ms.mio.service.process.MessageProcessor;
import cn.ms.mio.transport.AioSession;

/**
 * @author Seer
 * @version V1.0 , 2017/8/23
 */
public class IntegerServerProcessor implements MessageProcessor<Integer> {
    @Override
    public void process(AioSession<Integer> session, Integer msg) throws Exception {
        Integer respMsg=msg+1;
        System.out.println("接受到客户端数据：" + msg + " ,响应数据:" + (respMsg));
        session.write(respMsg);
    }

    @Override
    public void initSession(AioSession<Integer> session) {

    }
}
