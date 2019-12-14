package io.mio.aio2;

import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * AioMioContext
 *
 * @author lry
 */
@Data
@Slf4j
public class AioMioContext {

    private ByteBuffer readByteBuffer;
    private ByteBuffer writeByteBuffer;
    private AsynchronousSocketChannel socketChannel;
    private MioCallback<MioMessage> callback;

    public AioMioContext(AsynchronousSocketChannel socketChannel, MioCallback<MioMessage> callback) {
        this.socketChannel = socketChannel;
        this.callback = callback;
    }

    /**
     * The clear read or write ByteBuffer, and close socket channel
     */
    public void clearClose() {
        try {
            if (readByteBuffer != null) {
                readByteBuffer.clear();
                readByteBuffer = null;
            }
        } catch (Exception e) {
            log.error("Clear ByteBuffer exception", e);
        }

        try {
            if (writeByteBuffer != null) {
                writeByteBuffer.clear();
                writeByteBuffer = null;
            }
        } catch (Exception e) {
            log.error("Clear ByteBuffer exception", e);
        }

        try {
            if (socketChannel != null) {
                socketChannel.close();
                socketChannel = null;
            }
        } catch (Exception e) {
            log.error("Close client SocketChannel exception", e);
        }
    }

}
