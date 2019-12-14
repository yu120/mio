package io.mio.aio.plugin;

import io.mio.aio.EventState;
import io.mio.aio.MessageProcessor;
import io.mio.aio.NetFilter;
import io.mio.aio.support.AioSession;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMessageProcessor<T> implements MessageProcessor<T>, NetFilter<T> {

    private List<NetFilter<T>> plugins = new ArrayList<>();

    @Override
    public final void afterRead(AioSession<T> session, int readSize) {
        for (NetFilter<T> plugin : plugins) {
            plugin.afterRead(session, readSize);
        }
    }

    @Override
    public final void afterWrite(AioSession<T> session, int writeSize) {
        for (NetFilter<T> plugin : plugins) {
            plugin.afterWrite(session, writeSize);
        }
    }

    @Override
    public final void beforeRead(AioSession<T> session) {
        for (NetFilter<T> plugin : plugins) {
            plugin.beforeRead(session);
        }
    }

    @Override
    public final void beforeWrite(AioSession<T> session) {
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
                return accept;
            }
        }
        return true;
    }

    @Override
    public final void process(AioSession<T> session, T msg) {
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
     * 处理接收到的消息
     *
     * @param session
     * @param msg
     * @see MessageProcessor#process(AioSession, Object)
     */
    public abstract void process0(AioSession<T> session, T msg);

    /**
     * @param session          本次触发状态机的AioSession对象
     * @param eventState 状态枚举
     * @param throwable        异常对象，如果存在的话
     */
    @Override
    public final void stateEvent(AioSession<T> session, EventState eventState, Throwable throwable) {
        for (NetFilter<T> plugin : plugins) {
            plugin.stateEvent(eventState, session, throwable);
        }
        stateEvent0(session, eventState, throwable);
    }

    /**
     * @param session
     * @param eventState
     * @param throwable
     * @see #stateEvent(AioSession, EventState, Throwable)
     */
    public abstract void stateEvent0(AioSession<T> session, EventState eventState, Throwable throwable);

    public final void addPlugin(NetFilter plugin) {
        this.plugins.add(plugin);
    }

    @Override
    public boolean preProcess(AioSession<T> session, T t) {
        return false;
    }

    @Override
    public void stateEvent(EventState eventState, AioSession<T> session, Throwable throwable) {

    }

}
