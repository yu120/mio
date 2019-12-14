package io.mio.aio;

import io.mio.aio.support.EventState;
import io.mio.aio.support.TcpAioSession;

/**
 * 消息处理器。
 * <p>
 * 通过实现该接口，对完成解码的消息进行业务处理。
 *
 * @param <T> 消息对象实体类型
 * @author lry
 */
public interface MessageProcessor<T> {

    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    void process(TcpAioSession<T> session, T msg);

    /**
     * 状态机事件,当枚举事件发生时由框架触发该方法
     *
     * @param session    本次触发状态机的AioSession对象
     * @param eventState 状态枚举
     * @param throwable  异常对象，如果存在的话
     * @see EventState
     */
    void stateEvent(TcpAioSession<T> session, EventState eventState, Throwable throwable);

}
