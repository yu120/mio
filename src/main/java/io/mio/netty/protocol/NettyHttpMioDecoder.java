package io.mio.netty.protocol;

import io.mio.commons.MioConstants;
import io.mio.commons.MioMessage;
import io.mio.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NettyHttpMioDecoder
 *
 * @author lry
 */
public class NettyHttpMioDecoder extends MessageToMessageDecoder<FullHttpMessage> {

    private Serialize serialize;
    private ChannelPipeline pipeline;

    public NettyHttpMioDecoder(int maxContentLength, Serialize serialize, ChannelPipeline pipeline) {
        this.serialize = serialize;
        this.pipeline = pipeline;
        if (pipeline != null) {
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpMessage msg, List<Object> out) throws Exception {
        Channel channel = ctx.channel();

        // build uri
        String uri = null;
        if (msg instanceof FullHttpRequest) {
            uri = ((FullHttpRequest) msg).uri();
        }

        // parse header data
        Map<String, Object> headers = readHeaders(uri, msg.headers().entries());
        byte[] header = serialize.serialize(headers);
        int headerLength = header.length;

        // parse body data
        byte[] data = readByteBuf(msg.content());
        int dataLength = data.length;

        // build message
        final MioMessage mioMessage = MioMessage.build(headers, headerLength, dataLength, data);
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
     * The read header
     *
     * @param uri           http uri
     * @param headerEntries header entry list
     * @return header map
     */
    private Map<String, Object> readHeaders(String uri, List<Map.Entry<String, String>> headerEntries) {
        Map<String, Object> headers = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : headerEntries) {
            headers.put(entry.getKey(), entry.getValue());
        }

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

        return headers;
    }

}
