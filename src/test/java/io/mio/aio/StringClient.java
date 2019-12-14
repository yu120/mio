package io.mio.aio;

import io.mio.aio.plugin.AbstractMessageProcessor;
import io.mio.aio.plugin.MonitorFilter;
import io.mio.aio.buffer.BufferPagePool;
import io.mio.aio.support.AioSession;
import io.mio.aio.support.WriteBuffer;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;

public class StringClient {

    public static void main(String[] args) throws IOException {
        BufferPagePool bufferPagePool = new BufferPagePool(1024 * 1024 * 32, 10, true);
        AbstractMessageProcessor<String> processor = new AbstractMessageProcessor<String>() {
            @Override
            public void process0(AioSession<String> session, String msg) {

            }

            @Override
            public void stateEvent0(AioSession<String> session, EventState stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        };
        processor.addPlugin(new MonitorFilter(5));
        AsynchronousChannelGroup asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
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

    public static void test(AsynchronousChannelGroup asynchronousChannelGroup, BufferPagePool bufferPagePool, AbstractMessageProcessor<String> processor) throws InterruptedException, ExecutionException, IOException {
        AioMioClient<String> client = new AioMioClient<>("localhost", 8888, new StringProtocol(), processor);
        client.setBufferPagePool(bufferPagePool);
        client.setWriteQueueCapacity(10);
        client.setBufferPoolChunkSize(1024 * 1024);
        AioSession<String> session = client.start(asynchronousChannelGroup);
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
