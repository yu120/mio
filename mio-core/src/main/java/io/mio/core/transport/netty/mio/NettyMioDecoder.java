package io.mio.core.transport.netty.mio;

import io.mio.core.MioConstants;
import io.mio.core.MioMessage;
import io.mio.core.compress.Compress;
import io.mio.core.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * NettyMioDecoder
 * <p>
 * ===================================================================================================================================
 * [Protocol]：magic(1 byte) + serialize(1 byte) + attachment length(4 byte) + data length(4 byte) + attachment(M byte) + data(N byte)
 * ===================================================================================================================================
 * <p>
 * Tips：Decrypt the message flow to protocol data and add it to the online document
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyMioDecoder extends ByteToMessageDecoder {

    private final int maxContentLength;
    private final Serialize serialize;
    private final Compress compress;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        final Channel channel = ctx.channel();

        // Step 1：Readable length must be greater than basic length
        if (buffer.readableBytes() < MioConstants.BASE_READ_LENGTH) {
            return;
        }

        // Step 2：防止socket字节流攻击,防止客户端传来的数据过大。且太大数据是不合理的(单位字节:byte)
        if (buffer.readableBytes() > MioConstants.BASE_READ_LENGTH + maxContentLength) {
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
            if (buffer.readByte() == MioConstants.MAGIC_DATA) {
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

        // Step 4：Read version,length, and check content length
        byte version = buffer.readByte();
        int attachmentLength = buffer.readInt();
        int dataLength = buffer.readInt();
        // 判断请求数据包的剩余数据是否到齐
        if (buffer.readableBytes() < attachmentLength + dataLength) {
            // Restore read pointer
            buffer.readerIndex(beginReaderIndex);
            return;
        }

        // Step 5：Read attachments data
        byte[] attachmentBytes = new byte[attachmentLength];
        buffer.readBytes(attachmentBytes);

        // Step 6：Read attachments data
        byte[] dataBytes = new byte[dataLength];
        buffer.readBytes(dataBytes);

        // Step 7：uncompress data
        if (compress != null) {
            try {
                dataBytes = compress.uncompress(dataBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Step 8：build to output
        final MioMessage mioMessage = serialize.deserialize(dataBytes, MioMessage.class);
        mioMessage.decodeAttachments(attachmentBytes);
        mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());
        out.add(mioMessage);
    }

}