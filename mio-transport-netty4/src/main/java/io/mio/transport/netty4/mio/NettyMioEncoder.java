package io.mio.transport.netty4.mio;

import io.mio.core.MioConstants;
import io.mio.core.commons.MioException;
import io.mio.core.commons.MioMessage;
import io.mio.core.serialize.Serialize;
import io.mio.core.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * NettyMioEncoder
 *
 * <pre>
 * ====================================================================================================================
 * [Protocol]：magic(1 byte) + version(1 byte) + length(4 byte) + body(data+exception+attachments, N byte)+ xor(1 byte)
 * ====================================================================================================================
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
    private Serialize serialize;

    @Override
    protected void encode(ChannelHandlerContext ctx, final MioMessage msg, ByteBuf out) throws Exception {
        byte[] body = serialize.serialize(msg);

        final Channel channel = ctx.channel();
        int length = body.length;
        int contentLength = MioConstants.BASE_READ_LENGTH + length + MioConstants.XOR_BYTE;
        byte xor = ByteUtils.xor(msg.getVersion(), length, body);

        // wrapper local and remote address
        msg.wrapper(channel.localAddress(), channel.remoteAddress());
        if (contentLength > maxContentLength) {
            throw new MioException(MioException.CONTENT_OUT_LIMIT, "The content out of limit", contentLength);
        }

        // Step 1：write magic
        out.writeByte(MioConstants.MAGIC_DATA);
        // Step 2：write version
        out.writeByte(msg.getVersion());
        // Step 3：write length
        out.writeInt(length);
        // Step 4：write data
        out.writeBytes(body);
        // Step 5：write xor
        out.writeByte(xor);
    }

}