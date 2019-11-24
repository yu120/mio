package io.mio.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
 *
 * @author lry
 */
public class MioEncoder extends MessageToByteEncoder<MioProtocol> {

    @Override
    protected void encode(ChannelHandlerContext tcx, MioProtocol msg, ByteBuf out) throws Exception {
        // 写入消息的具体内容

        // 1.写入消息的开头的信息标志(int类型)
        out.writeInt(msg.getHeadData());
        // 2.写入消息的长度(int 类型)
        out.writeInt(msg.getContentLength());
        // 3.写入消息的内容(byte[]类型)
        out.writeBytes(msg.getContent());
    }

}