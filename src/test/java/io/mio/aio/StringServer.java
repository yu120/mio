package io.mio.aio;

import io.mio.aio.plugin.AbstractMessageProcessor;
import io.mio.aio.plugin.MonitorFilter;
import lombok.extern.slf4j.Slf4j;
import io.mio.aio.support.AioSession;
import io.mio.aio.support.WriteBuffer;

import java.io.IOException;

@Slf4j
public class StringServer {

    public static void main(String[] args) throws IOException {
        AbstractMessageProcessor<String> processor = new AbstractMessageProcessor<String>() {
            @Override
            public void process0(AioSession<String> session, String msg) {
                WriteBuffer outputStream = session.writeBuffer();

                try {
                    byte[] bytes = msg.getBytes();
                    outputStream.writeInt(bytes.length);
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void stateEvent0(AioSession<String> session, EventState stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    log.error(stateMachineEnum + " exception:", throwable);
                }
            }
        };

        AioMioServer<String> server = new AioMioServer<>(8888, new StringProtocol(), processor);
        server.setReadBufferSize(1024 * 1024)
                .setThreadNum(Runtime.getRuntime().availableProcessors() + 1)
                .setBufferPoolPageSize(1024 * 1024 * 16)
                .setBufferPoolChunkSize(4096);
        processor.addPlugin(new MonitorFilter(5));
        server.start();
    }

}
