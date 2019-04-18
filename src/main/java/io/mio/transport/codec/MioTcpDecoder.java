package io.mio.transport.codec;

import io.mio.commons.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Mio TCP Decoder
 *
 * @author lry
 */
public class MioTcpDecoder extends ByteToMessageDecoder {

    /**
     * 1.headData占据4个字节
     * 2.headerLength占据4个字节
     * 3.contentLength占据4个字节
     */
    private static final int BASE_LENGTH = 4 + 4 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        if (BASE_LENGTH <= buffer.readableBytes()) {
            int beginReader;
            while (true) {
                beginReader = buffer.readerIndex();
                buffer.markReaderIndex();
                if (Constants.HEAD_DATA == buffer.readInt()) {
                    break;
                }

                buffer.resetReaderIndex();
                buffer.readByte();
                if (BASE_LENGTH > buffer.readableBytes()) {
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

            out.add(new MioTcpMessage(header, content));
        }
    }
}
