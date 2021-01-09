package io.mio.core.transport.netty.http;

import io.mio.core.MioConstants;
import io.mio.core.MioMessage;
import io.mio.core.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpEncoder
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyHttpServerEncoder extends MessageToMessageEncoder<MioMessage> {

    private final Serialize serialize;

    @Override
    protected void encode(ChannelHandlerContext ctx, MioMessage msg, List<Object> out) throws Exception {
        byte[] body = serialize.serialize(msg);

        // convert data to ByteBuf
        ByteBuf content = Unpooled.wrappedBuffer(body);
        // setter response status
        Object status = msg.getAttachments().getOrDefault(MioConstants.RESPONSE_STATUS_KEY, HttpResponseStatus.OK.code());
        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf((int) status);

        // server send response encoder
        FullHttpMessage httpMessage = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus, content);

        // set auto header parameter
        HttpHeaders httpHeaders = httpMessage.headers();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, Object> entry : msg.getAttachments().entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }

        // set must be header parameter
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, httpMessage.content().readableBytes());
        httpHeaders.set(HttpHeaderNames.HOST, ((InetSocketAddress) ctx.channel().localAddress()).getHostString());
        if (!httpHeaders.contains(HttpHeaderNames.CONTENT_TYPE.toString())) {
            httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        }

        // add out context
        out.add(httpMessage);
    }

}
