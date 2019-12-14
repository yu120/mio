package io.mio.aio2.protocol;

import io.mio.MioMessage;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * AioMioDecoder
 *
 * @author lry
 */
public class AioMioDecoder {

    /**
     * The decode {@link ByteBuffer}
     *
     * @param socketChannel    {@link AsynchronousSocketChannel}
     * @param byteBuffer       {@link ByteBuffer}
     * @param mioMessage{@link MioMessage}
     */
    public void decode(AsynchronousSocketChannel socketChannel, ByteBuffer byteBuffer, MioMessage mioMessage) {
        byteBuffer.flip();
        // Retrieve bytes between the position and limit
        // (see Putting Bytes into a ByteBuffer)
        byte[] bytes = new byte[byteBuffer.remaining()];
        // transfer bytes from this buffer into the given destination array
        byteBuffer.get(bytes, 0, bytes.length);
        byteBuffer.clear();
    }

}

