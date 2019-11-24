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
        // 可读长度必须大于基本长度
        if (buffer.readableBytes() >= MioProtocol.BASE_LENGTH) {
            // 防止socket字节流攻击,防止，客户端传来的数据过大。因为，太大的数据，是不合理的
            if (buffer.readableBytes() > MioProtocol.MAX_LENGTH) {
                buffer.skipBytes(buffer.readableBytes());
            }

            // 记录包头开始的index
            int beginReader;
            while (true) {
                // 获取包头开始的index
                beginReader = buffer.readerIndex();
                // 标记包头开始的index（把当前读指针保存起来）
                buffer.markReaderIndex();
                // 读到了协议的开始标志，结束while循环
                if (buffer.readInt() == MioProtocol.HEAD_DATA) {
                    break;
                }

                // 把当前读指针恢复到之前保存的值
                buffer.resetReaderIndex();
                // 未读到包头，则跳过一个字节。每次跳过一个字节去读取包头信息的开始标记
                buffer.readByte();

                // 当跳过一个字节之后，数据包的长度又变得不满足。此时应该结束,等待后面的数据到达
                if (buffer.readableBytes() < MioProtocol.BASE_LENGTH) {
                    return;
                }
            }

            // 消息的长度
            int length = buffer.readInt();
            // 判断请求数据包数据是否到齐
            if (buffer.readableBytes() < length) {
                // 还原读指针
                buffer.readerIndex(beginReader);
                return;
            }

            // 读取data数据
            byte[] data = new byte[length];
            buffer.readBytes(data);
            out.add(new MioProtocol(data.length, data));
        }
    }

}