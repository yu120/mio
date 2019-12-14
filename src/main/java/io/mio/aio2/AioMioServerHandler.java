package io.mio.aio2;

import io.mio.commons.MioMessage;
import io.mio.aio2.protocol.AioMioDecoder;
import io.mio.aio2.protocol.AioMioEncoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AioMioServerHandler
 * <p>
 * Tip：Accept到单个请求时的回调处理
 *
 * @author lry
 */
@Slf4j
public class AioMioServerHandler implements CompletionHandler<AsynchronousSocketChannel, AioMioServer> {

    private AioMioDecoder decoder;
    private AioMioEncoder encoder;

    public AioMioServerHandler() {
        this.decoder = new AioMioDecoder();
        this.encoder = new AioMioEncoder();
    }

    @Override
    public void completed(AsynchronousSocketChannel clientSocketChannel, AioMioServer attachment) {
        try {
            // 检查通道
            if (clientSocketChannel.isOpen()) {
                AioMioContext aioMioContext = new AioMioContext(clientSocketChannel, attachment.getMioCallback());
                readByteBuffer(aioMioContext);
            }
        } finally {
            // 监听新的请求，递归调用。不重新设置接收的话就只能接收一次
            attachment.getServerSocketChannel().accept(attachment, this);
        }
    }

    @Override
    public void failed(Throwable exc, AioMioServer attachment) {
        try {
            log.error("Server failed", exc);
        } finally {
            // 监听新的请求，递归调用(不重新设置接收的话就只能接收1次)
            attachment.getServerSocketChannel().accept(attachment, this);
        }
    }

    private void readByteBuffer(AioMioContext aioMioContext) {
        AsynchronousSocketChannel socketChannel = aioMioContext.getSocketChannel();
        aioMioContext.setReadByteBuffer(ByteBuffer.allocate(1024));

        socketChannel.read(aioMioContext.getReadByteBuffer(), aioMioContext, new CompletionHandler<Integer, AioMioContext>() {
            @Override
            public void completed(Integer result, AioMioContext attachment) {
                // 分流处理不同
                if (result < 0) {
                    log.warn("Client close connection channel");
                    attachment.clearClose();
                } else if (result == 0) {
                    log.warn("Received blank data");
                } else {
                    try {
                        MioMessage mioMessage = MioMessage.buildEmpty();
                        decoder.decode(socketChannel, attachment.getReadByteBuffer(), mioMessage);
                        attachment.getCallback().onProcessor(msg -> writeByteBuffer(attachment, msg), mioMessage);
                    } catch (Exception e) {
                        log.error("Server read completed exception", e);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, AioMioContext attachment) {
                attachment.clearClose();
                log.error("Server read client failed", exc);
            }
        });
    }

    private void writeByteBuffer(AioMioContext aioMioContext, MioMessage mioMessage) {
        AsynchronousSocketChannel socketChannel = aioMioContext.getSocketChannel();
        ByteBuffer writeByteBuffer = ByteBuffer.allocateDirect(1024);
        encoder.encode(socketChannel, mioMessage, writeByteBuffer);

        socketChannel.write(writeByteBuffer, aioMioContext, new CompletionHandler<Integer, AioMioContext>() {
            @Override
            public void completed(Integer result, AioMioContext attachment) {
                attachment.clearClose();
            }

            @Override
            public void failed(Throwable exc, AioMioContext attachment) {
                log.error("Server write failed", exc);
                attachment.clearClose();
            }
        });
    }

}
