package io.mio.transport.netty4.http;

import io.mio.core.commons.MioConstants;
import io.mio.core.commons.MioMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpEncoder
 *
 * @author lry
 */
public class NettyHttpClientEncoder extends MessageToMessageEncoder<MioMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MioMessage msg, List<Object> out) throws Exception {
        // setter request method
        Object requestMethod = msg.getHeaders().getOrDefault(MioConstants.REQUEST_METHOD_KEY, HttpMethod.POST.name());
        HttpMethod httpMethod = HttpMethod.valueOf(String.valueOf(requestMethod));
        // setter uri
        Object path = msg.getHeaders().getOrDefault(MioConstants.URI_KEY, "/");
        String uri = new URI(String.valueOf(path)).toASCIIString();
        // setter content
        ByteBuf content = Unpooled.wrappedBuffer(msg.getData());
        FullHttpMessage httpMessage = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri, content);

        // set auto header parameter
        HttpHeaders httpHeaders = httpMessage.headers();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, Object> entry : msg.getHeaders().entrySet()) {
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
