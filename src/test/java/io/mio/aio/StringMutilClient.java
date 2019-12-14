package io.mio.aio;

import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.plugin.AbstractMessageProcessor;
import io.mio.aio.support.TcpAioSession;
import io.mio.aio.support.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class StringMutilClient {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        BufferPagePool bufferPagePool = new BufferPagePool(1024 * 1024 * 32, 10, true);
        AbstractMessageProcessor<String> processor = new AbstractMessageProcessor<String>() {
            @Override
            public void process0(TcpAioSession<String> session, String msg) {

            }

            @Override
            public void stateEvent0(TcpAioSession<String> session, EventState stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        };

        AioMioClient<String> client = new AioMioClient<>("localhost", 8888, new StringProtocol(), processor);
        client.setBufferPagePool(bufferPagePool);
        client.setWriteQueueCapacity(20);
        TcpAioSession<String> session = client.start();
        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        WriteBuffer outputStream = session.writeBuffer();
                        byte[] data = "mio-aio".getBytes();
                        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
                        buffer.putInt(data.length);
                        buffer.put(data);
                        byte[] a = buffer.array();
                        while (true) {

                            outputStream.write(a);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }.start();
        }
    }

}
