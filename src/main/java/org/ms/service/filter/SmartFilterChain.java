package org.ms.service.filter;

import org.ms.transport.AioSession;

/**
 * 业务层消息预处理器
 *
 * @author Seer
 */
public interface SmartFilterChain<T> {

    void doChain(AioSession<T> session, T buffer, int readSize);
}
