package cn.ms.mio.service.filter;

import cn.ms.mio.transport.AioSession;

/**
 * 业务层消息预处理器
 *
 * @author Seer
 */
public interface SmartFilterChain<T> {

    void doChain(AioSession<T> session, T buffer, int readSize);
}
