package io.mio.aio;

import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.filter.MonitorFilter;
import io.mio.aio.support.*;

import java.io.IOException;
import java.nio.ByteBuffer;

public class StringMutilClient {

    public static void main(String[] args) throws Exception {
        MessageProcessor<String> processor = new MessageProcessor<String>() {
            @Override
            public void process0(AioMioSession<String> session, String msg) {

            }

            @Override
            public void stateEvent0(AioMioSession<String> session, EventState stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        };
        processor.addFilter(new MonitorFilter(5));
        BufferPagePool bufferPagePool = new BufferPagePool(1024 * 1024 * 32, 10, true);

        AioClientConfig<String> config = new AioClientConfig<>();
        config.setHostname("localhost");
        config.setPort(8888);
        config.setWriteQueueCapacity(20);
        config.setBufferPoolChunkSize(1024 * 1024);

        AioMioClient<String> client = new AioMioClient<>(config, new StringProtocol(), processor);
        client.setBufferPool(bufferPagePool);
        AioMioSession<String> session = client.start();

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
