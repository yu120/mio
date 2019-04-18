package io.mio.transport.codec;

import io.netty.channel.ChannelHandler;

import java.util.List;

/**
 * Codec
 *
 * @author lry
 */
public interface ICodec<T> {

    /**
     * Server decode -> encode
     *
     * @return
     */
    List<ChannelHandler> decodeEncode();

    /**
     * Client encode -> decode
     *
     * @return
     */
    List<ChannelHandler> encodeDecode();

}
