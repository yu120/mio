package io.mio.transport.netty4.mio;

import io.mio.core.commons.MioConstants;
import io.mio.core.commons.MioException;
import io.mio.core.commons.MioMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * NettyMioEncoder
 *
 * <pre>
 * ===============================================================================================================
 * [Protocol]： magic(1 byte) + attachmentLength(4 byte) + dataLength(4 byte) + attachment(M byte) + data(N byte)
 * ===============================================================================================================
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

    private int maxContentLength;

    @Override
    protected void encode(ChannelHandlerContext ctx, final MioMessage msg, ByteBuf out) throws Exception {
        final Channel channel = ctx.channel();

        int attachmentLength = msg.getAttachment().length;
        int dataLength = msg.getData().length;

        // wrapper local and remote address
        msg.wrapper(channel.localAddress(), channel.remoteAddress());
        if (attachmentLength + dataLength > maxContentLength) {
            throw new MioException(MioException.CONTENT_OUT_LIMIT, "The content out of limit", dataLength);
        }

        // Step 1：write magic
        out.writeByte(MioConstants.MAGIC_DATA);
        // Step 2：write attachment length
        out.writeInt(attachmentLength);
        // Step 3：write data length
        out.writeInt(dataLength);
        // Step 4：write attachment data
        out.writeBytes(msg.getAttachment());
        // Step 5：write data
        out.writeBytes(msg.getData());
    }

}