package cn.ms.mio.service.filter;

import cn.ms.mio.transport.MioSession;

/**
 * 业务层消息预处理器
 *
 * @author Seer
 */
public interface SmartFilter<T> {

    /**
     * 消息处理前置预处理
     *
     * @param session
     * @param d
     */
    public void processFilter(MioSession<T> session, T d);

    /**
     * 消息接受前置预处理
     *
     * @param session
     * @param d
     */
    public void readFilter(MioSession<T> session, T d, int readSize);

    /**
     * 消息接受失败处理
     */
    public void processFailHandler(MioSession<T> session, T d);

}
