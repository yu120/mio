package io.mio.aio;

import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.filter.MonitorFilter;
import io.mio.aio.support.*;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ThreadFactory;

public class StringClient {

    public static void main(String[] args) throws IOException {
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
        AsynchronousChannelGroup asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ClientGroup");
                    }
                });

        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        test(asynchronousChannelGroup, bufferPagePool, processor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    public static void test(AsynchronousChannelGroup asynchronousChannelGroup,
                            BufferPagePool bufferPagePool, MessageProcessor<String> processor) throws Exception {
        AioClientConfig<String> config = new AioClientConfig<>();
        config.setHostname("localhost");
        config.setPort(8888);
        config.setWriteQueueCapacity(10);
        config.setBufferPoolChunkSize(1024 * 1024);

        AioMioClient<String> client = new AioMioClient<>(config, new StringProtocol(), processor);
        client.setBufferPool(bufferPagePool);

        AioMioSession<String> session = client.start(asynchronousChannelGroup);
        WriteBuffer outputStream = session.writeBuffer();
        byte[] data = "mio-aio".getBytes();
        while (true) {
            int num = (int) (Math.random() * 10) + 1;
            outputStream.writeInt(data.length * num);
            while (num-- > 0) {
                outputStream.write(data);
            }
        }
    }

}
