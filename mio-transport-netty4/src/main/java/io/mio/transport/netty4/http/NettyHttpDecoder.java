package io.mio.transport.netty4.http;

import io.mio.core.commons.MioConstants;
import io.mio.core.commons.MioMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpDecoder
 *
 * @author lry
 */
public class NettyHttpDecoder extends MessageToMessageDecoder<FullHttpMessage> {

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpMessage msg, List<Object> out) throws Exception {
        Channel channel = ctx.channel();

        final Map<String, Object> headers = new LinkedHashMap<>();
        // parse header data
        for (Map.Entry<String, String> entry : msg.headers().entries()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            // build uri
            readUriParameters(headers, request.uri());
            // parse set uri
            headers.put(MioConstants.REQUEST_METHOD_KEY, request.method().name());
        } else if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            // parse set uri
            headers.put(MioConstants.RESPONSE_STATUS_KEY, response.status().code());
        }

        // parse body data
        byte[] data = readByteBuf(msg.content());

        // build message
        final MioMessage mioMessage = new MioMessage(headers, null, data);
        mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());
        out.add(mioMessage);
    }

    /**
     * The read {@link ByteBuf}
     *
     * @param byteBuf {@link ByteBuf}
     * @return byte[]
     */
    private byte[] readByteBuf(ByteBuf byteBuf) {
        byte[] byteData = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, byteData);
        return byteData;
    }

    /**
     * The read uri and parameters
     *
     * @param headers headers
     * @param uri     http uri
     */
    private void readUriParameters(Map<String, Object> headers, String uri) {
        // parse set uri
        headers.put(MioConstants.URI_KEY, uri);

        // parse query parameter
        if (uri != null && uri.trim().length() > 0) {
            Map<String, Object> parameters = new LinkedHashMap<>();
            QueryStringDecoder qsd = new QueryStringDecoder(uri);
            for (Map.Entry<String, List<String>> entry : qsd.parameters().entrySet()) {
                List<String> values = entry.getValue();
                if (values == null || values.size() == 0) {
                    continue;
                }
                parameters.put(entry.getKey(), values.get(0));
            }
            headers.put(MioConstants.PARAMETERS_KEY, parameters);
        }
    }

}
