package io.mio.core.transport.netty.http;

import io.mio.core.MioConstants;
import io.mio.core.MioMessage;
import io.mio.core.serialize.Serialize;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpDecoder
 * <p>
 * 1.server request decode
 * 2.client response decode
 *
 * @author lry
 */
@AllArgsConstructor
public class NettyHttpDecoder extends MessageToMessageDecoder<FullHttpMessage> {

    private final Serialize serialize;

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
        byte[] body = new byte[msg.content().readableBytes()];
        msg.content().getBytes(0, body);

        // build message
        final MioMessage mioMessage = serialize.deserialize(body, MioMessage.class);
        mioMessage.wrapper(channel.localAddress(), channel.remoteAddress());
        out.add(mioMessage);
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
