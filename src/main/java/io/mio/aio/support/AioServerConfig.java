package io.mio.aio.support;

import lombok.Data;

import java.io.Serializable;
import java.net.SocketOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AioServerConfig
 *
 * @author lry
 */
@Data
public class AioServerConfig implements Serializable {

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
     * Socket的TCP参数配置
     * <p>
     * AIO服务端的有效可选范围为：
     * 1. StandardSocketOptions.SO_RCVBUF
     * 2. StandardSocketOptions.SO_REUSEADDR
     */
    private Map<SocketOption<Object>, Object> socketOptions = new LinkedHashMap<>();

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

}
