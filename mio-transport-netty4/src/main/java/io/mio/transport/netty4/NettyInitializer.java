package io.mio.transport.netty4;

import io.mio.core.compress.Compress;
import io.mio.core.extension.SPI;
import io.mio.core.serialize.Serialize;

/**
 * NettyInitializer
 *
 * @author lry
 */
@SPI("mio")
public interface NettyInitializer<A> {

    /**
     * The initialize
     *
     * @param maxContentLength max content length
     * @param serialize        {@link Serialize}
     * @param compress         {@link Compress}
     */
    void initialize(boolean server, int maxContentLength, Serialize serialize, Compress compress, A attachment);

}