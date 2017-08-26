package cn.ms.mio.filter;

import cn.ms.mio.transport.support.MioSession;

/**
 * 业务层消息预处理器
 *
 * @author lry
 */
public interface MioFilterChain<T> {

    void doChain(MioSession<T> session, T buffer, int readSize);
}
