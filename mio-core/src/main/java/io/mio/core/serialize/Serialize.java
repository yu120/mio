package io.mio.core.serialize;

import io.mio.core.extension.SPI;

import java.io.IOException;

/**
 * The Data Serialize.
 *
 * @author lry
 */
@SPI("hessian2")
public interface Serialize {

    /**
     * The serialize object
     *
     * @param object object implements java.io.Serializable
     * @return byte[] data
     * @throws IOException IO exception {@link IOException}
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * The deserialize object
     *
     * @param bytes byte[] data
     * @param clz   {@link T} class
     * @return {@link T}
     * @throws IOException IO exception {@link IOException}
     */
    <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException;

    /**
     * The serialize multi object
     *
     * @param objects object array implements java.io.Serializable
     * @return byte[] data
     * @throws IOException IO exception {@link IOException}
     */
    byte[] serializeMulti(Object[] objects) throws IOException;

    /**
     * The deserialize multi object
     *
     * @param bytes   byte[] data
     * @param classes object class array
     * @return object array
     * @throws IOException IO exception {@link IOException}
     */
    Object[] deserializeMulti(byte[] bytes, Class<?>[] classes) throws IOException;

}
