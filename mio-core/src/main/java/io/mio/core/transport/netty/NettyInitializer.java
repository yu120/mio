package io.mio.core.transport.netty;

import io.mio.core.compress.Compress;
import io.mio.core.extension.SPI;
import io.mio.core.serialize.Serialize;

/**
 * NettyInitializer
 *
 * @author lry
 */
@SPI("mio")
public interface NettyInitializer {

    /**
     * The initialize
     *
     * @param maxContentLength max content length
     * @param serialize        {@link Serialize}
     * @param compress         {@link Compress}
     */
    void server(int maxContentLength, Serialize serialize, Compress compress, Object attachment);

    void client(int maxContentLength, Serialize serialize, Compress compress, Object attachment);

}
