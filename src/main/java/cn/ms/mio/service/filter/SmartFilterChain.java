package cn.ms.mio.service.filter;

import cn.ms.mio.transport.MioSession;

/**
 * 业务层消息预处理器
 *
 * @author Seer
 */
public interface SmartFilterChain<T> {

    void doChain(MioSession<T> session, T buffer, int readSize);
}
