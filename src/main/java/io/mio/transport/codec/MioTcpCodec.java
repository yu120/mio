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
     * 1.headData占据4个字节
     * 2.headerLength占据4个字节
     * 3.contentLength占据4个字节
     */
    public static final int BASE_LENGTH = 4 + 4 + 4;
    public static final int MAX_LENGTH = 2048;

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

                    // 读取请求头长度
                    int headerLength = buffer.readInt();
                    // 读取请求体长度
                    int contentLength = buffer.readInt();

                    // 判断请求数据包数据是否到齐
                    if (buffer.readableBytes() < headerLength + contentLength) {
                        buffer.readerIndex(beginReader);
                        return;
                    }

                    // 读取Header
                    byte[] header = new byte[headerLength];
                    buffer.readBytes(header, beginReader, beginReader + headerLength);

                    // 读取Content
                    byte[] content = new byte[contentLength];
                    buffer.readBytes(content);

                    out.add(new MioTcpProtocol(header, content));
                }
            }

        };
    }

    @Override
    public MessageToByteEncoder encode() {
        return new MessageToByteEncoder<MioTcpProtocol>() {

            @Override
            protected void encode(ChannelHandlerContext tcx, MioTcpProtocol msg, ByteBuf out) throws Exception {
                out.writeInt(msg.getHeadData());
                out.writeInt(msg.getHeaderLength());
                out.writeInt(msg.getContentLength());
                out.writeBytes(msg.getHeader());
                out.writeBytes(msg.getContent());
            }

        };
    }

}
