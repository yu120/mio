package io.mio.transport.codec;

import io.mio.commons.Constants;
import io.mio.transport.protocol.MioTcpProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

/**
 * 编解码器
 *
 * @author lry
 */
public class MioTcpCodec implements ICodec<ChannelHandler> {

    /**
     * 1.协议开始的标准headData，int类型，占据4个字节.
     * 2.表示数据的长度contentLength，int类型，占据4个字节.
     */
    public final int BASE_LENGTH = 4 + 4;
    public final int MAX_LENGTH = 2048;

    @Override
    public ByteToMessageDecoder decode() {
        return new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
                // 可读长度必须大于基本长度
                if (buffer.readableBytes() >= BASE_LENGTH) {
                    // 1.防止socket字节流攻击
                    // 2.防止，客户端传来的数据过大
                    // 3.因为，太大的数据，是不合理的
                    if (buffer.readableBytes() > MAX_LENGTH) {
                        buffer.skipBytes(buffer.readableBytes());
                    }

                    // 记录包头开始的index
                    int beginReader;
                    while (true) {
                        // 获取包头开始的index
                        beginReader = buffer.readerIndex();
                        // 标记包头开始的index
                        buffer.markReaderIndex();
                        // 读到了协议的开始标志，结束while循环
                        if (buffer.readInt() == Constants.HEAD_DATA) {
                            break;
                        }

                        // 未读到包头，略过一个字节
                        // 每次略过，一个字节，去读取，包头信息的开始标记
                        buffer.resetReaderIndex();
                        buffer.readByte();

                        // 当略过，一个字节之后，
                        // 数据包的长度，又变得不满足
                        // 此时，应该结束。等待后面的数据到达
                        if (buffer.readableBytes() < BASE_LENGTH) {
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
                    out.add(new MioTcpProtocol(data.length, data));
                }
            }
        };
    }

    @Override
    public MessageToByteEncoder encode() {
        return new MessageToByteEncoder<MioTcpProtocol>() {
            @Override
            protected void encode(ChannelHandlerContext tcx, MioTcpProtocol msg, ByteBuf out) throws Exception {
                // 1.写入消息的开头的信息标志(int类型)
                out.writeInt(msg.getHeadData());
                // 2.写入消息的长度(int 类型)
                out.writeInt(msg.getContentLength());
                // 3.写入消息的内容(byte[]类型)
                out.writeBytes(msg.getContent());
            }

        };
    }

}
