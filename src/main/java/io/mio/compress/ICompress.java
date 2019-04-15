package io.mio.compress;

import java.io.IOException;

/**
 * Data Compress
 *
 * @author lry
 */
public interface ICompress {

    /**
     * The Data compress.
     *
     * @param data byte array data
     * @return byte array data
     * @throws IOException IO exception
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * The Data uncompress.
     *
     * @param data byte array data
     * @return byte array data
     * @throws IOException IO exception
     */
    byte[] unCompress(byte[] data) throws IOException;

}
