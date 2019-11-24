package io.mio.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——-----——-+——-----——+——----——+
 * | 协议开始标志 |  长度       |   数据     |
 * +——----——-+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据
 * </pre>
 *
 * @author lry
 */
@Data
public class MioProtocol implements Serializable {

    /**
     * 协议开始的标准head_data，int类型，占据4个字节.
     * 表示数据的长度contentLength，int类型，占据4个字节.
     */
    public static final int BASE_LENGTH = 4 + 4;
    public static final int HEAD_DATA = 0x76;
    /**
     * 最大10MB
     */
    public static final int MAX_LENGTH = 10 * 1024 * 1024;

    /**
     * 消息的开头的信息标志
     */
    private int headData = HEAD_DATA;
    /**
     * 消息的长度
     */
    private int contentLength;
    /**
     * 消息的内容
     */
    private byte[] content;

    /**
     * 用于初始化，MioProtocol
     *
     * @param contentLength 协议里面，消息数据的长度
     * @param content       协议里面，消息的数据
     */
    public MioProtocol(int contentLength, byte[] content) {
        this.contentLength = contentLength;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MioProtocol{" +
                "headData=" + headData +
                ", contentLength=" + contentLength +
                ", content=" + new String(content) +
                '}';
    }

}
