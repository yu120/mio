package io.mio;

import io.mio.extension.SPI;

/**
 * Codec
 *
 * @author lry
 */
@SPI("mio")
public interface Codec<A> {

    /**
     * The server decode and encode
     *
     * @param maxContentLength max content length
     * @param serialize        {@link Serialize}
     * @param attachment       {@link A}
     */
    void server(int maxContentLength, Serialize serialize, A attachment);

    /**
     * The client decode and encode
     *
     * @param maxContentLength max content length
     * @param serialize        {@link Serialize}
     * @param attachment       {@link A}
     */
    void client(int maxContentLength, Serialize serialize, A attachment);

}
