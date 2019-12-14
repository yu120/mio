package io.mio.aio2;

import io.mio.aio2.protocol.AioMioDecoder;
import io.mio.aio2.protocol.AioMioEncoder;
import io.mio.commons.MioCallback;
import io.mio.commons.MioMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AioMioClientHandler
 *
 * @author lry
 */
@Slf4j
public class AioMioClientHandler implements CompletionHandler<Void, AioMioClient> {

    private AioMioClient attachment;
    private AioMioDecoder decoder;
    private AioMioEncoder encoder;

    public AioMioClientHandler() {
        this.decoder = new AioMioDecoder();
        this.encoder = new AioMioEncoder();
    }

    @Override
    public void completed(Void result, AioMioClient attachment) {
        log.info("客户端成功连接到服务器...");
        attachment.getCountDownLatch().countDown();
        this.attachment = attachment;
    }

    @Override
    public void failed(Throwable exc, AioMioClient attachment) {
        log.info("连接服务器失败...", exc);

        try {
            attachment.getSocketChannel().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callback(final MioMessage mioMessage, final MioCallback<MioMessage> callback) throws Throwable {
        AsynchronousSocketChannel socketChannel = attachment.getSocketChannel();
        if (socketChannel.isOpen()) {
            AioMioContext aioMioContext = new AioMioContext(socketChannel, callback);
            writeByteBuffer(aioMioContext, mioMessage);
        }
    }

    private void writeByteBuffer(AioMioContext aioMioContext, MioMessage mioMessage) {
        AsynchronousSocketChannel socketChannel = attachment.getSocketChannel();
        ByteBuffer writeByteBuffer = ByteBuffer.allocateDirect(1024);
        encoder.encode(socketChannel, mioMessage, writeByteBuffer);
        aioMioContext.setWriteByteBuffer(writeByteBuffer);

        socketChannel.write(aioMioContext.getWriteByteBuffer(), aioMioContext, new CompletionHandler<Integer, AioMioContext>() {
            @Override
            public void completed(Integer result, AioMioContext attachment) {
                if (attachment.getWriteByteBuffer().hasRemaining()) {
                    //完成全部数据的写入
                    socketChannel.write(aioMioContext.getWriteByteBuffer(), aioMioContext, this);
                } else {
                    //读取数据
                    readByteBuffer(aioMioContext);
                }
            }

            @Override
            public void failed(Throwable exc, AioMioContext attachment) {
                log.error("Server write failed", exc);
                attachment.clearClose();
            }
        });
    }

    private void readByteBuffer(AioMioContext attachment) {
        attachment.setReadByteBuffer(ByteBuffer.allocate(1024));
        AsynchronousSocketChannel socketChannel = attachment.getSocketChannel();

        socketChannel.read(attachment.getReadByteBuffer(), attachment, new CompletionHandler<Integer, AioMioContext>() {
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
                        // 解码
                        MioMessage mioMessage = MioMessage.buildEmpty();
                        decoder.decode(socketChannel, attachment.getReadByteBuffer(), mioMessage);
                        attachment.getCallback().onSuccess(mioMessage);
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

}
