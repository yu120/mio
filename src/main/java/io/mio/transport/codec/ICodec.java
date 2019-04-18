package io.mio.transport.codec;

/**
 * 编解码器
 *
 * @author lry
 */
public interface ICodec<T> {

    /**
     * ByteBuf decoder
     *
     * @return decoder
     */
    T decoder();

    /**
     * Object encoder
     *
     * @return encoder
     */
    T encoder();

}
