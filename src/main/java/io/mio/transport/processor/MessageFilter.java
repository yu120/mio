package io.mio.transport.processor;

import io.mio.transport.MioSession;

/**
 * 业务层消息预处理器
 * 
 * @author lry
 */
public interface MessageFilter<T> {

    /**
     * 消息处理前置预处理
     *
     * @param session
     * @param d
     */
    void processFilter(MioSession<T> session, T d);

    /**
     * 消息接受前置预处理
     *
     * @param session
     * @param d
     */
    void readFilter(MioSession<T> session, T d, int readSize);

    /**
     * 消息接受失败处理
     */
    void processFailHandler(MioSession<T> session, T d,Exception e);

}
