package io.mio.aio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * AioConstants
 *
 * @author lry
 */
@Slf4j
public final class AioConstants {

    /**
     * The close
     *
     * @param channel 需要被关闭的通道
     */
    public static void close(AsynchronousSocketChannel channel) {
        if (channel == null) {
            throw new NullPointerException();
        }
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            log.debug("shutdown input exception", e);
        }
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            log.debug("shutdown output exception", e);
        }
        try {
            channel.close();
        } catch (IOException e) {
            log.debug("close channel exception", e);
        }
    }

}
