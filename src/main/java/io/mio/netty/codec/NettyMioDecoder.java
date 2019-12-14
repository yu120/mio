package io.mio.netty.codec;

import io.mio.commons.MioConstants;
import io.mio.commons.MioMessage;
import io.mio.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * NettyMioDecoder
 *
 * <pre>
 * ===============================================================================================================
 * [Protocol]： head(1 byte) + headerLength(4 byte) + headerDataLength(4 byte) + header(M byte) + data(N byte)
 * ===============================================================================================================
 * Consider:
 * 6.crc data(crc), cyclic redundancy detection.The XOR algorithm is used to
 * calculate whether the whole packet has errors during transmission
 * </pre>
 * <p>
 * Tips：Decrypt the message flow to protocol data and add it to the online document
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyMioDecoder extends ByteToMessageDecoder {

    private int maxContentLength;
    private Serialize serialize;

    @SuppressWarnings("unchecked")
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        Channel channel = ctx.channel();

        // Step 1：Readable length must be greater than basic length
        if (buffer.readableBytes() < MioConstants.BASE_READ_LENGTH) {
            return;
        }

        // Step 2：防止socket字节流攻击,防止客户端传来的数据过大。且太大数据是不合理的(单位字节:byte)
        if (buffer.readableBytes() > maxContentLength) {
            buffer.skipBytes(buffer.readableBytes());
            return;
        }

        // Step 3：记录读包头开始的索引位置（原理:循环查找满足读的数据包头索引位置进行读取）
        int beginReaderIndex;
        while (true) {
            // Step 3.1：获取包头开始的index
            beginReaderIndex = buffer.readerIndex();

            // Step 3.2：标记包头开始的index（把当前读指针保存起来）
            buffer.markReaderIndex();
            if (buffer.readByte() == MioConstants.HEAD_DATA) {
                // 读到了协议的开始标志，则结束while循环
                break;
            } else {
                // 把当前读指针恢复到之前保存的值(即还原上述buffer.readInt()操作)
                buffer.resetReaderIndex();
            }

            // Step 3.3：未读到包头信息，则跳过一个字节后再去读取包头信息的开始标志(即逐一尝试去读取)
            buffer.readByte();
            if (buffer.readableBytes() < MioConstants.BASE_READ_LENGTH) {
                // 当跳过一个字节之后，数据包的长度如果变得不满足，则应该结束,等待后面的数据到达
                return;
            }
        }

        // Step 4：读取后续数据总长度（循环中读取完包头）
        int headerLength = buffer.readInt();
        int contentLength = buffer.readInt();
        int dataLength = contentLength - headerLength;

        // Step 5：判断请求数据包数据是否到齐
        if (buffer.readableBytes() < contentLength) {
            // Restore read pointer
            buffer.readerIndex(beginReaderIndex);
            return;
        }

        // Step 6：Read head meta length and head meta data
        byte[] header = new byte[headerLength];
        buffer.readBytes(header);
        Map<String, Object> headers = serialize.deserialize(header, Map.class);

        // Step 7：Read body data
        byte[] data = new byte[dataLength];
        buffer.readBytes(data);

        // Step 8：Add to output
        final MioMessage mioMessage = new MioMessage(headers, data);
        mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());
        out.add(mioMessage);
    }

}