package io.mio.aio2.protocol;

import io.mio.commons.MioMessage;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * AioMioEncoder
 *
 * @author lry
 */
public class AioMioEncoder {

    /**
     * The encode {@link MioMessage}
     *
     * @param socketChannel {@link AsynchronousSocketChannel}
     * @param mioMessage    {@link MioMessage}
     * @param byteBuffer    {@link ByteBuffer}
     */
    public void encode(AsynchronousSocketChannel socketChannel, MioMessage mioMessage, ByteBuffer byteBuffer) {
        byteBuffer.put(mioMessage.getData());
    }

}
