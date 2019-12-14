package io.mio.aio.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * AioSession
 *
 * @param <T> 消息对象类型
 * @author lry
 */
public abstract class AioSession<T> {

    /**
     * Session状态:已关闭
     */
    protected static final byte SESSION_STATUS_CLOSED = 1;
    /**
     * Session状态:关闭中
     */
    protected static final byte SESSION_STATUS_CLOSING = 2;
    /**
     * Session状态:正常
     */
    protected static final byte SESSION_STATUS_ENABLED = 3;

    /**
     * 会话当前状态
     *
     * @see AioSession#SESSION_STATUS_CLOSED
     * @see AioSession#SESSION_STATUS_CLOSING
     * @see AioSession#SESSION_STATUS_ENABLED
     */
    protected byte status = SESSION_STATUS_ENABLED;
    /**
     * 附件对象
     */
    private Object attachment;

    /**
     * 获取WriteBuffer用以数据输出
     *
     * @return WriteBuffer
     */
    public abstract WriteBuffer writeBuffer();

    /**
     * 强制关闭当前AIOSession。
     * <p>若此时还存留待输出的数据，则会导致该部分数据丢失</p>
     */
    public final void close() {
        close(true);
    }

    /**
     * 是否立即关闭会话
     *
     * @param immediate true:立即关闭,false:响应消息发送完后关闭
     */
    public abstract void close(boolean immediate);

    /**
     * 获取当前Session的唯一标识
     *
     * @return sessionId
     */
    public String getSessionId() {
        return "aioSession-" + hashCode();
    }

    /**
     * 当前会话是否已失效
     *
     * @return 是否失效
     */
    public boolean isInvalid() {
        return status != SESSION_STATUS_ENABLED;
    }


    /**
     * 获取附件对象
     *
     * @param <T> 附件对象类型
     * @return 附件
     */
    public final <T> T getAttachment() {
        return (T) attachment;
    }

    /**
     * 存放附件，支持任意类型
     *
     * @param <T>        附件对象类型
     * @param attachment 附件对象
     */
    public final <T> void setAttachment(T attachment) {
        this.attachment = attachment;
    }

    /**
     * 获取当前会话的本地连接地址
     *
     * @return 本地地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public abstract InetSocketAddress getLocalAddress() throws IOException;

    /**
     * 获取当前会话的远程连接地址
     *
     * @return 远程地址
     * @throws IOException IO异常
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public abstract InetSocketAddress getRemoteAddress() throws IOException;

}
