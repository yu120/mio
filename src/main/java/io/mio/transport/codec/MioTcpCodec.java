package io.mio.transport.codec;

import io.netty.channel.ChannelHandler;

import java.util.Arrays;
import java.util.List;

/**
 * 编解码器
 *
 * @author lry
 */
public class MioTcpCodec implements ICodec<ChannelHandler> {

    @Override
    public List<ChannelHandler> decodeEncode() {
        return Arrays.asList(new MioTcpDecoder(), new MioTcpEncoder());
    }

    @Override
    public List<ChannelHandler> encodeDecode() {
        return Arrays.asList(new MioTcpEncoder(), new MioTcpDecoder());
    }

}
