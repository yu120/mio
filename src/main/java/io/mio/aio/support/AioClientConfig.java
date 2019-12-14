package io.mio.aio.support;

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
public class AioClientConfig<T> {

    /**
     * 消息体缓存大小,字节。设置读缓存区大小。单位：byte
     */
    private int readBufferSize = 512;

    /**
     * Write缓存区容量
     */
    private int writeQueueCapacity = 512;
    /**
     * 远程服务器IP
     */
    private String hostname;
    /**
     * 服务器端口号
     */
    private int port = 8888;

    /**
     * Socket 配置
     */
    private Map<SocketOption<Object>, Object> socketOptions;

    /**
     * 线程数。设置服务工作线程数,设置数值必须大于等于2
     */
    private int threadNum = Math.min(Runtime.getRuntime().availableProcessors(), 2);

    /**
     * 内存页大小。设置单个内存页大小.多个内存页共同组成内存池
     */
    private int bufferPoolPageSize = 4096;

    /**
     * 共享缓存页大小
     */
    private int bufferPoolSharedPageSize = -1;

    /**
     * 内存页个数。设置内存页个数，多个内存页共同组成内存池。
     */
    private int bufferPoolPageNum = -1;

    /**
     * 内存块大小限制。限制写操作时从内存页中申请内存块的大小
     */
    private int bufferPoolChunkSize = 128;

    /**
     * 设置内存池是否使用直接缓冲区。true:直接缓冲区,false:堆内缓冲区
     */
    private boolean bufferPoolDirect = true;
    /**
     * 客户端连接超时时间，单位:毫秒
     */
    private int connectTimeout;

    public Map<SocketOption<Object>, Object> getSocketOptions() {
        return socketOptions;
    }

    /**
     * 设置Socket的TCP参数配置。
     * <p>
     * AIO客户端的有效可选范围为：
     * 1. StandardSocketOptions.SO_SNDBUF
     * 2. StandardSocketOptions.SO_RCVBUF
     * 3. StandardSocketOptions.SO_KEEPALIVE
     * 4. StandardSocketOptions.SO_REUSEADDR
     * 5. StandardSocketOptions.TCP_NODELAY
     *
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
