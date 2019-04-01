package io.mio.serialize;

import java.io.IOException;

/**
 * Serialize
 *
 * @author lry
 */
public interface ISerialize {

    /**
     * The serialize object data
     *
     * @param data object data
     * @return byte data array
     * @throws IOException io exception
     */
    byte[] serialize(Object data) throws IOException;

    /**
     * The deserialize byte data
     *
     * @param data object data
     * @param clz  object class
     * @param <T>  object class
     * @return deserialize object
     * @throws IOException io exception
     */
    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;

    /**
     * The serialize multiple object data
     *
     * @param data multiple data array
     * @return byte data array
     * @throws IOException io exception
     */
    byte[] serializeMulti(Object[] data) throws IOException;

    /**
     * The deserialize multiple byte data
     *
     * @param data    multiple data array
     * @param classes object class list
     * @return deserialize object
     * @throws IOException io exception
     */
    Object[] deserializeMulti(byte[] data, Class<?>[] classes) throws IOException;

}
