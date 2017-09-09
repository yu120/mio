package cn.ms.mio.transport.support;

import cn.ms.mio.transport.processor.MessageProcessor;
import cn.ms.mio.transport.processor.MessageFilter;
import cn.ms.mio.transport.protocol.Protocol;

/**
 * 服务端/客户端配置信息 T:解码后生成的对象类型，S:
 * 
 * @author lry
 */
public final class MioConf<T> {

    /**
     * 消息队列缓存大小
     */
    private int writeQueueSize = 512;

    /**
     * 消息体缓存大小,字节
     */
    private int readBufferSize = 512;

    /**
     * 远程服务器IP
     */
    private String host;


    /**
     * 服务器消息拦截器
     */
    private MessageFilter<T>[] filters;

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
     * 服务器处理线程数
     */
    private int threadNum = Runtime.getRuntime().availableProcessors();

    /**
     * true:服务器,false:客户端
     */
    private boolean serverOrClient;


    private float limitRate = 0.9f;

    private float releaseRate = 0.6f;
    /**
     * 流控指标线
     */
    private int flowLimitLine = (int) (writeQueueSize * limitRate);

    /**
     * 释放流控指标线
     */
    private int releaseLine = (int) (writeQueueSize * releaseRate);

    /**
     * @param serverOrClient true:服务器,false:客户端
     */
    public MioConf(boolean serverOrClient) {
        this.serverOrClient = serverOrClient;
    }

    public final String getHost() {
        return host;
    }


    public final int getPort() {
        return port;
    }


    public final int getThreadNum() {
        return threadNum;
    }


    public final void setHost(String host) {
        this.host = host;
    }


    public final void setPort(int port) {
        this.port = port;
    }


    public final void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }


    public final boolean isServer() {
        return serverOrClient;
    }

    public final MessageFilter<T>[] getFilters() {
        return filters;
    }

    public final void setFilters(MessageFilter<T>[] filters) {
        this.filters = filters;
    }

    public Protocol<T> getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol<T> protocol) {
        this.protocol = protocol;
    }

    public final MessageProcessor<T> getProcessor() {
        return processor;
    }

    public final void setProcessor(MessageProcessor<T> processor) {
        this.processor = processor;
    }

    public int getWriteQueueSize() {
        return writeQueueSize;
    }

    public void setWriteQueueSize(int writeQueueSize) {
        this.writeQueueSize = writeQueueSize;
        flowLimitLine = (int) (writeQueueSize * limitRate);
        releaseLine = (int) (writeQueueSize * releaseRate);
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public int getFlowLimitLine() {
        return flowLimitLine;
    }

    public int getReleaseLine() {
        return releaseLine;
    }
    
}
