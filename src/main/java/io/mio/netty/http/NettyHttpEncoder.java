package io.mio.netty.http;

import io.mio.commons.MioConstants;
import io.mio.commons.MioMessage;
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
public class NettyHttpEncoder extends MessageToMessageEncoder<MioMessage> {

    private boolean server;

    public NettyHttpEncoder(boolean server) {
        this.server = server;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MioMessage msg, List<Object> out) throws Exception {
        // convert data to ByteBuf
        ByteBuf content = Unpooled.wrappedBuffer(msg.getData());

        FullHttpMessage httpMessage;
        if (server) {
            // server send response encoder
            httpMessage = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        } else {
            // client send request encoder
            Object path = msg.getHeaders().getOrDefault(MioConstants.URI_KEY, "/");
            Object requestMethod = msg.getHeaders().getOrDefault(MioConstants.REQUEST_METHOD_KEY, HttpMethod.POST.name());

            HttpMethod httpMethod = HttpMethod.valueOf(String.valueOf(requestMethod));
            String uri = new URI(String.valueOf(path)).toASCIIString();
            httpMessage = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri, content);
        }

        // set auto header parameter
        HttpHeaders httpHeaders = httpMessage.headers();
        if (!httpHeaders.isEmpty()) {
            for (Map.Entry<String, Object> entry : msg.getHeaders().entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }


        // client encoder
        // httpHeaders.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
        // server encoder
        // httpHeaders.set(HttpHeaderNames.CONTENT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);

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
