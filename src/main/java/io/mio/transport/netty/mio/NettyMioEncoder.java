package io.mio.transport.netty.mio;

import io.mio.commons.MioConstants;
import io.mio.commons.MioException;
import io.mio.commons.MioMessage;
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
 * [Protocol]： head(1 byte) + headerLength(4 byte) + headerDataLength(4 byte) + header(M byte) + data(N byte)
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
        Channel channel = ctx.channel();

        // serialize header data
        int headerLength = msg.getHeader().length;
        int dataLength = msg.getData().length;

        // wrapper local and remote address
        msg.wrapper(channel.localAddress(), channel.remoteAddress());
        int headerDataLength = headerLength + dataLength;
        if (headerDataLength > maxContentLength) {
            throw new MioException(MioException.CONTENT_OUT_LIMIT, "The content out of limit", dataLength);
        }

        // Step 1： command head
        out.writeByte(MioConstants.HEAD_DATA);
        // Step 2：head meta length
        out.writeInt(headerLength);
        // Step 3：all content data length
        out.writeInt(headerDataLength);
        // Step 4：head meta data
        out.writeBytes(msg.getHeader());
        // Step 5：body data
        out.writeBytes(msg.getData());
    }

}