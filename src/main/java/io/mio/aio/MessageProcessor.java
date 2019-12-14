package io.mio.aio;

import io.mio.aio.support.AioMioSession;
import io.mio.aio.support.EventState;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息处理器。
 * <p>
 * 通过实现该接口，对完成解码的消息进行业务处理。
 *
 * @param <T> 消息对象实体类型
 * @author lry
 */
public class MessageProcessor<T> implements NetFilter<T> {

    private List<NetFilter<T>> plugins = new ArrayList<>();

    @Override
    public final void afterRead(AioMioSession<T> session, int readSize) {
        for (NetFilter<T> plugin : plugins) {
            plugin.afterRead(session, readSize);
        }
    }

    @Override
    public final void afterWrite(AioMioSession<T> session, int writeSize) {
        for (NetFilter<T> plugin : plugins) {
            plugin.afterWrite(session, writeSize);
        }
    }

    @Override
    public final void beforeRead(AioMioSession<T> session) {
        for (NetFilter<T> plugin : plugins) {
            plugin.beforeRead(session);
        }
    }

    @Override
    public final void beforeWrite(AioMioSession<T> session) {
        for (NetFilter<T> plugin : plugins) {
            plugin.beforeWrite(session);
        }
    }

    @Override
    public final boolean shouldAccept(AsynchronousSocketChannel channel) {
        boolean accept;
        for (NetFilter<T> plugin : plugins) {
            accept = plugin.shouldAccept(channel);
            if (!accept) {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    public final void process(AioMioSession<T> session, T msg) {
        boolean flag = true;
        for (NetFilter<T> plugin : plugins) {
            if (!plugin.preProcess(session, msg)) {
                flag = false;
            }
        }
        if (flag) {
            process0(session, msg);
        }
    }

    /**
     * 状态机事件,当枚举事件发生时由框架触发该方法
     *
     * @param session    本次触发状态机的AioMioSession对象
     * @param eventState 状态枚举
     * @param throwable  异常对象，如果存在的话
     * @see EventState
     */
    public final void stateEvent(AioMioSession<T> session, EventState eventState, Throwable throwable) {
        for (NetFilter<T> plugin : plugins) {
            plugin.stateEvent(eventState, session, throwable);
        }
        stateEvent0(session, eventState, throwable);
    }

    public final void addFilter(NetFilter filter) {
        this.plugins.add(filter);
    }

    @Override
    public boolean preProcess(AioMioSession<T> session, T t) {
        return false;
    }

    @Override
    public void stateEvent(EventState eventState, AioMioSession<T> session, Throwable throwable) {

    }

    /**
     * 处理接收到的消息
     *
     * @param session {@link AioMioSession}
     * @param msg     {@link T}
     * @see MessageProcessor#process(AioMioSession, Object)
     */
    public void process0(AioMioSession<T> session, T msg) {

    }

    /**
     * The state event
     *
     * @param session    {@link AioMioSession}
     * @param eventState {@link EventState}
     * @param throwable  {@link Throwable}
     * @see #stateEvent(AioMioSession, EventState, Throwable)
     */
    public void stateEvent0(AioMioSession<T> session, EventState eventState, Throwable throwable) {

    }

}
