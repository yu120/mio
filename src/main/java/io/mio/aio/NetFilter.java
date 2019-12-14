package io.mio.aio;

import io.mio.aio.support.AioSession;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * 网络监控器，提供通讯层面监控功能的接口。
 * <p>
 * mio-aio并未单独提供配置监控服务的接口，用户在使用时仅需在MessageProcessor实现类中同时实现当前NetMonitor接口即可。
 * 在注册消息处理器时，若服务监测到该处理器同时实现了NetMonitor接口，则该监视器便会生效。
 * </p>
 * <h2>示例：</h2>
 * <pre>
 *     public class MessageProcessorImpl implements MessageProcessor,NetFilter{
 *
 *     }
 * </pre>
 *
 * <b>注意:</b>
 * <p>
 * 实现本接口时要关注acceptMonitor接口的返回值,如无特殊需求直接返回true，若返回false会拒绝本次连接。
 * </p>
 * <b>非必要情况下请勿使用该接口，未来可能会调整接口设计</b>
 *
 * @param <T> 消息对象实体类型
 * @author lry
 */
public interface NetFilter<T> {

    /**
     * <p>
     * 监控已接收到的连接
     * </p>
     *
     * @param channel 当前已经建立连接的通道对象
     * @return true:接受该连接,false:拒绝该连接
     */
    boolean shouldAccept(AsynchronousSocketChannel channel);

    /**
     * 监控触发本次读回调Session的已读数据字节数
     *
     * @param session  当前执行read的AioSession对象
     * @param readSize 已读数据长度
     */
    void afterRead(AioSession<T> session, int readSize);

    /**
     * 即将开始读取数据
     *
     * @param session 当前会话对象
     */
    void beforeRead(AioSession<T> session);

    /**
     * 监控触发本次写回调session的已写数据字节数
     *
     * @param session   本次执行write回调的AIOSession对象
     * @param writeSize 本次输出的数据长度
     */
    void afterWrite(AioSession<T> session, int writeSize);

    /**
     * 即将开始写数据
     *
     * @param session 当前会话对象
     */
    void beforeWrite(AioSession<T> session);

    /**
     * 对请求消息进行预处理，并决策是否进行后续的MessageProcessor处理。
     * 若返回false，则当前消息将被忽略。
     * 若返回true，该消息会正常秩序MessageProcessor.process.
     *
     * @param session
     * @param t
     * @return
     */
    boolean preProcess(AioSession<T> session, T t);

    /**
     * 监听状态机事件
     *
     * @param eventState
     * @param session
     * @param throwable
     * @see MessageProcessor#stateEvent(AioSession, EventState, Throwable)
     */
    void stateEvent(EventState eventState, AioSession<T> session, Throwable throwable);

}
