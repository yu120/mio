package io.mio.core.serialize;

import io.mio.core.extension.SPI;

import java.io.IOException;

/**
 * The Data Serialize.
 *
 * @author lry
 */
@SPI(value = "hessian2", single = true)
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

}
