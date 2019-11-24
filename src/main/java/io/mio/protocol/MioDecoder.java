package io.mio.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度             |   数据       |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据,长度不应该超过2048，防止socket流的攻击
 * </pre>
 *
 * @author lry
 */
public class MioDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        System.out.println("================");
        // Step 1：可读长度必须大于基本长度
        if (buffer.readableBytes() < MioProtocol.BASE_LENGTH) {
            return;
        }

        // Step 2：防止socket字节流攻击,防止客户端传来的数据过大。而且太大的数据是不合理的
        if (buffer.readableBytes() > MioProtocol.MAX_LENGTH) {
            buffer.skipBytes(buffer.readableBytes());
        }

        // Step 3：记录读包头开始的索引位置（原理:循环查找满足读的数据包头索引位置进行读取）
        int beginReaderIndex;
        while (true) {
            // Step 3.1：获取包头开始的index
            beginReaderIndex = buffer.readerIndex();

            // Step 3.2：标记包头开始的index（把当前读指针保存起来）
            buffer.markReaderIndex();
            if (buffer.readInt() == MioProtocol.HEAD_DATA) {
                // 读到了协议的开始标志，则结束while循环
                break;
            } else {
                // 把当前读指针恢复到之前保存的值(即还原上述buffer.readInt()操作)
                buffer.resetReaderIndex();
            }

            // Step 3.3：未读到包头信息，则跳过一个字节后再去读取包头信息的开始标志(即逐一尝试去读取)
            buffer.readByte();
            if (buffer.readableBytes() < MioProtocol.BASE_LENGTH) {
                // 当跳过一个字节之后，数据包的长度如果变得不满足，则应该结束,等待后面的数据到达
                return;
            }
        }

        // Step 4：读取消息的长度（循环中读取完包头）
        int length = buffer.readInt();

        // Step 5：判断请求数据包数据是否到齐
        if (buffer.readableBytes() < length) {
            // 还原读指针
            buffer.readerIndex(beginReaderIndex);
            return;
        }

        // Step 6：读取data数据
        byte[] data = new byte[length];
        buffer.readBytes(data);
        out.add(new MioProtocol(length, data));
    }

}