package io.mio.aio.support;

import io.mio.aio.MessageProcessor;
import io.mio.aio.Protocol;

import java.nio.channels.CompletionHandler;
import java.nio.ByteBuffer;

/**
 * 列举了当前mio-aio所关注的各类状态枚举。
 *
 * @author lry
 */
public enum EventState {

    /**
     * 连接已建立并构建Session对象
     */
    NEW_SESSION,
    /**
     * 读通道已被关闭。
     * <p>
     * 通常由以下几种情况会触发该状态：
     * 1.对端主动关闭write通道，致使本通常满足了EOF条件
     * 2.当前AioMioSession处理完读操作后检测到自身正处于{@link EventState#SESSION_CLOSING}状态
     * <p>
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    INPUT_SHUTDOWN,
    /**
     * 业务处理异常。
     * <p>
     * 执行{@link MessageProcessor#process(AioMioSession, Object)}期间发生用户未捕获的异常。
     */
    PROCESS_EXCEPTION,

    /**
     * 协议解码异常。
     * <p>
     * 执行{@link Protocol#decode(ByteBuffer, AioMioSession)}期间发生未捕获的异常。
     */
    DECODE_EXCEPTION,
    /**
     * 读操作异常。
     * <p>
     * 在底层服务执行read操作期间因发生异常情况出发了{@link CompletionHandler#failed(Throwable, Object)}。
     * <p>
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    INPUT_EXCEPTION,
    /**
     * 写操作异常。
     * <p>
     * 在底层服务执行write操作期间因发生异常情况出发了{@link CompletionHandler#failed(Throwable, Object)}。
     * <p>
     * 未来该状态机可能会废除，并转移至NetMonitor
     */
    OUTPUT_EXCEPTION,
    /**
     * 会话正在关闭中
     * <p>
     * 执行了{@link AioMioSession#close(boolean false)}方法，并且当前还存在待输出的数据。
     */
    SESSION_CLOSING,
    /**
     * 会话关闭成功
     */
    SESSION_CLOSED,
    /**
     * 拒绝接受连接(Server端)
     */
    REJECT_ACCEPT

}
