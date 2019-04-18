package io.mio.transport.protocol;

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
public class MioTcpProtocol {
    /**
     * 消息的开头的信息标志
     */
    private int headData = Constants.HEAD_DATA;
    /**
     * 消息的长度
     */
    private int contentLength;
    /**
     * 消息的内容
     */
    private byte[] content;

    /**
     * 用于初始化，MioTcpProtocol
     *
     * @param contentLength 协议里面，消息数据的长度
     * @param content       协议里面，消息的数据
     */
    public MioTcpProtocol(int contentLength, byte[] content) {
        this.contentLength = contentLength;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MioTcpProtocol [headData=" + headData + ", contentLength="
                + contentLength + ", content=" + new String(content) + "]";
    }

}
