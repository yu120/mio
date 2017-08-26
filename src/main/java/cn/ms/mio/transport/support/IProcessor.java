package cn.ms.mio.transport.support;


/**
 * 消息处理器
 *
 * @author lry
 */
public interface IProcessor<T> {

    /**
     * 用于处理指定session内的一个消息实例,若直接在该方法内处理消息,则实现的是同步处理方式.
     * 若需要采用异步，则介意此方法的实现仅用于接收消息，至于消息处理则在其他线程中实现
     *
     * @param session
     * @throws Exception
     */
    void process(MioSession<T> session, T msg) throws Exception;

    /**
     * 初始化业务层Session对象
     *
     * @param session 传输层会话
     * @return
     */
    void initSession(MioSession<T> session);
    
}
