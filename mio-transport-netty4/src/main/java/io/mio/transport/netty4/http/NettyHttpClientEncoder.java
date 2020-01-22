package io.mio.transport.netty4.http;

import io.mio.core.MioConstants;
import io.mio.core.commons.MioMessage;
import io.mio.core.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpEncoder
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyHttpClientEncoder extends MessageToMessageEncoder<MioMessage> {

    private final Serialize serialize;

    @Override
    protected void encode(ChannelHandlerContext ctx, MioMessage msg, List<Object> out) throws Exception {
        byte[] body = serialize.serialize(msg);

        // setter request method
        Object requestMethod = msg.getAttachments().getOrDefault(MioConstants.REQUEST_METHOD_KEY, HttpMethod.POST.name());
        HttpMethod httpMethod = HttpMethod.valueOf(String.valueOf(requestMethod));
        // setter uri
        Object path = msg.getAttachments().getOrDefault(MioConstants.URI_KEY, "/");
        String uri = new URI(String.valueOf(path)).toASCIIString();
        // setter content
        ByteBuf content = Unpooled.wrappedBuffer(body);
        FullHttpMessage httpMessage = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri, content);

        // set auto header parameter
        HttpHeaders httpHeaders = httpMessage.headers();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, Object> entry : msg.getAttachments().entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }

        // set must be header parameter
        httpHeaders.set(HttpHeaderNames.HOST, ((InetSocketAddress) ctx.channel().localAddress()).getHostString());
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, httpMessage.content().readableBytes());
        if (!httpHeaders.contains(HttpHeaderNames.CONTENT_TYPE.toString())) {
            httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        }

        // add out context
        out.add(httpMessage);
    }

}
