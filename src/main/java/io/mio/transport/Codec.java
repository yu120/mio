package io.mio.transport;

import io.mio.commons.extension.SPI;

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
     * @param attachment       {@link A}
     */
    void server(int maxContentLength, A attachment);

    /**
     * The client decode and encode
     *
     * @param maxContentLength max content length
     * @param attachment       {@link A}
     */
    void client(int maxContentLength, A attachment);

}
