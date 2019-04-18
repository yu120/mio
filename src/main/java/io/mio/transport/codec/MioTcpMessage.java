package io.mio.transport.codec;

import io.mio.commons.Constants;
import lombok.Data;

import java.util.Arrays;

/**
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度             |   数据       |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据
 * </pre>
 */
@Data
public class MioTcpMessage {
    /**
     * 消息的开头的信息标志
     */
    private int headData = Constants.HEAD_DATA;
    /**
     * 消息的长度
     */
    private int headerLength;
    /**
     * 消息的长度
     */
    private int contentLength;
    /**
     * 消息请求头的内容
     */
    private byte[] header;
    /**
     * 消息请求体的内容
     */
    private byte[] content;

    /**
     * 用于初始化，MioTcpMessage
     *
     * @param header  协议里面，消息的数据
     * @param content 协议里面，消息的数据
     */
    public MioTcpMessage(byte[] header, byte[] content) {
        this.headerLength = header.length;
        this.contentLength = content.length;
        this.header = header;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MioTcpMessage [headData=" + headData + ", headerLength=" + headerLength + ", contentLength="
                + contentLength + ", header=" + new String(header) + ", content=" + new String(content) + "]";
    }

}
