package cn.ms.mio.service.filter.impl;

import cn.ms.mio.service.filter.MioFilter;
import cn.ms.mio.service.filter.MioFilterChain;
import cn.ms.mio.service.process.IProcessor;
import cn.ms.mio.transport.support.MioSession;

/**
 * 业务层消息预处理器
 *
 * @author lry
 */
public class DefaultMioFilterChain<T> implements MioFilterChain<T> {
 
	private IProcessor<T> receiver;
    private MioFilter<T>[] handlers = null;
    private boolean withoutFilter = true;//是否无过滤器

    public DefaultMioFilterChain(IProcessor<T> receiver, MioFilter<T>[] handlers) {
        this.receiver = receiver;
        this.handlers = handlers;
        this.withoutFilter = handlers == null || handlers.length == 0;
    }

    public void doChain(MioSession<T> session, T dataEntry, int readSize) {
        if (dataEntry == null) {
            return;
        }
        if (withoutFilter) {
            try {
                receiver.process(session, dataEntry);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // 接收到的消息进行预处理
        for (MioFilter<T> h : handlers) {
            h.readFilter(session, dataEntry, readSize);
        }
        try {
            for (MioFilter<T> h : handlers) {
                h.processFilter(session, dataEntry);
            }
            receiver.process(session, dataEntry);
        } catch (Exception e) {
            e.printStackTrace();
            for (MioFilter<T> h : handlers) {
                h.processFailHandler(session, dataEntry);
            }
        }
    }
    
}
