package cn.ms.mio.filter;

import cn.ms.mio.transport.support.MioSession;

/**
 * 业务层消息预处理器
 *
 * @author lry
 */
public interface MioFilter<T> {

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
    void processFailHandler(MioSession<T> session, T d);

}
