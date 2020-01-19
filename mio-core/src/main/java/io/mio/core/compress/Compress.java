package io.mio.core.compress;

import io.mio.core.extension.SPI;

import java.io.IOException;

/**
 * The Data Compress/UnCompress.
 *
 * @author lry
 */
@SPI("gzip")
public interface Compress {

    /**
     * The Data compress.
     *
     * @param data byte[] data
     * @return compress byte[] data
     * @throws IOException exception {@link IOException}
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * The Data uncompress.
     *
     * @param data byte[] data
     * @return compress byte[] data
     * @throws IOException exception {@link IOException}
     */
    byte[] uncompress(byte[] data) throws IOException;

}
