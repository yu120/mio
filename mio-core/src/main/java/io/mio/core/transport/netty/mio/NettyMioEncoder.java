package io.mio.core.transport.netty.mio;

import io.mio.core.MioConstants;
import io.mio.core.MioException;
import io.mio.core.MioMessage;
import io.mio.core.compress.Compress;
import io.mio.core.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * NettyMioEncoder
 *
 * <pre>
 * ===================================================================================================================================
 * [Protocol]：magic(1 byte) + serialize(1 byte) + attachment length(4 byte) + data length(4 byte) + attachment(M byte) + data(N byte)
 * ===================================================================================================================================
 * Consider:
 * 6.crc data(crc), cyclic redundancy detection.The XOR algorithm is used to
 * calculate whether the whole packet has errors during transmission
 * </pre>
 * <p>
 * Tips：Write protocol data to message flow.
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyMioEncoder extends MessageToByteEncoder<MioMessage> {

    public static final int VERSION = 1;

    private final int maxContentLength;
    private final Serialize serialize;
    private final Compress compress;

    @Override
    protected void encode(ChannelHandlerContext ctx, final MioMessage msg, ByteBuf out) throws Exception {
        byte[] attachmentBytes = msg.encodeAttachments();
        byte[] dataBytes = serialize.serialize(msg.getData());
        if (compress != null) {
            dataBytes = compress.compress(dataBytes);
        }

        int contentLength = attachmentBytes.length + dataBytes.length;
        if (contentLength > maxContentLength) {
            throw new MioException(MioException.CONTENT_OUT_LIMIT, "The content out of limit", contentLength);
        }

        // wrapper local and remote address
        final Channel channel = ctx.channel();
        msg.wrapper(channel.localAddress(), channel.remoteAddress());

        // Step 1：write magic
        out.writeByte(MioConstants.MAGIC_DATA);
        // Step 2：write serialize
        out.writeByte(VERSION);
        // Step 3：write attachment length
        out.writeInt(attachmentBytes.length);
        // Step 4：write data length
        out.writeInt(dataBytes.length);
        // Step 5：write attachment
        out.writeBytes(attachmentBytes);
        // Step 6：write data
        out.writeBytes(dataBytes);
    }

}