package io.mio.aio.support;

import io.mio.aio.MessageProcessor;
import io.mio.aio.NetFilter;
import io.mio.aio.Protocol;
import lombok.Data;

import java.net.SocketOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Quickly服务端/客户端配置信息
 *
 * @param <T> 解码后生成的对象类型
 * @author lry
 */
@Data
public class IoServerConfig<T> {

    /**
     * 消息体缓存大小,字节
     */
    private int readBufferSize = 512;

    /**
     * Write缓存区容量
     */
    private int writeQueueCapacity = 512;
    /**
     * 远程服务器IP
     */
    private String host;
    /**
     * 服务器消息拦截器
     */
    private NetFilter<T> monitor;
    /**
     * 服务器端口号
     */
    private int port = 8888;
    /**
     * 消息处理器
     */
    private MessageProcessor<T> processor;
    /**
     * 协议编解码
     */
    private Protocol<T> protocol;

    /**
     * Socket 配置
     */
    private Map<SocketOption<Object>, Object> socketOptions;

    /**
     * 线程数
     */
    private int threadNum = 1;

    /**
     * 内存页大小
     */
    private int bufferPoolPageSize = 4096;

    /**
     * 共享缓存页大小
     */
    private int bufferPoolSharedPageSize = -1;

    /**
     * 内存页个数
     */
    private int bufferPoolPageNum = -1;

    /**
     * 内存块大小限制
     */
    private int bufferPoolChunkSize = 128;

    /**
     * 是否使用直接缓冲区内存
     */
    private boolean bufferPoolDirect = true;


    /**
     * @param processor 消息处理器
     */
    public void setProcessor(MessageProcessor<T> processor) {
        this.processor = processor;
        this.monitor = (processor instanceof NetFilter) ? (NetFilter<T>) processor : null;
    }


    public Map<SocketOption<Object>, Object> getSocketOptions() {
        return socketOptions;
    }

    /**
     * @param socketOption socketOption名称
     * @param f            socketOption值
     */
    public void setOption(SocketOption socketOption, Object f) {
        if (socketOptions == null) {
            socketOptions = new HashMap<>(4);
        }
        socketOptions.put(socketOption, f);
    }

}
