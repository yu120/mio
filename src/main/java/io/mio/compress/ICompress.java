package io.mio.compress;

import java.io.IOException;

public interface ICompress {

    /**
     * The Data compress.
     *
     * @param data
     * @return
     * @throws IOException
     */
    byte[] compress(byte[] data) throws IOException;

    /**
     * The Data uncompress.
     *
     * @param data
     * @return
     * @throws IOException
     */
    byte[] unCompress(byte[] data) throws IOException;

}
