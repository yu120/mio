package io.mio.transport.codec;

/**
 * 编解码器
 *
 * @author lry
 */
public interface ICodec<T> {

    T decode();

    T encode();

}
