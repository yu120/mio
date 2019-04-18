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
    private static final int BASE_LENGTH = 4 + 4 + 4;

    @Override
    public ByteToMessageDecoder decoder() {
        return new ByteToMessageDecoder() {

            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
                if (buffer.readableBytes() >= BASE_LENGTH) {
                    int beginReader;
                    while (true) {
                        beginReader = buffer.readerIndex();
                        buffer.markReaderIndex();
                        if (buffer.readInt() == Constants.HEAD_DATA) {
                            break;
                        }

                        buffer.resetReaderIndex();
                        buffer.readByte();
                        if (buffer.readableBytes() < BASE_LENGTH) {
                            return;
                        }
                    }

                    int headerLength = buffer.readInt();
                    int contentLength = buffer.readInt();
                    if (buffer.readableBytes() < headerLength + contentLength) {
                        buffer.readerIndex(beginReader);
                        return;
                    }

                    byte[] header = new byte[headerLength];
                    buffer.readBytes(header, beginReader, beginReader + headerLength);
                    byte[] content = new byte[contentLength];
                    buffer.readBytes(content);

                    out.add(new MioTcpProtocol(header, content));
                }
            }

        };
    }

    @Override
    public MessageToByteEncoder encoder() {
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
