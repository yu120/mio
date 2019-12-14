package io.mio.aio;

import io.mio.aio.filter.MonitorFilter;
import io.mio.aio.support.AioMioSession;
import io.mio.aio.support.EventState;
import io.mio.aio.support.WriteBuffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StringServer {

    public static void main(String[] args) throws IOException {
        MessageProcessor<String> processor = new MessageProcessor<String>() {
            @Override
            public void process0(AioMioSession<String> session, String msg) {
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
            public void stateEvent0(AioMioSession<String> session, EventState stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    log.error(stateMachineEnum + " exception:", throwable);
                }
            }
        };

        AioMioServer<String> server = new AioMioServer<>(8888, new StringProtocol(), processor);
        server.getConfig().setReadBufferSize(1024 * 1024);
        server.getConfig().setThreadNum(Runtime.getRuntime().availableProcessors() + 1);
        server.getConfig().setBufferPoolPageSize(1024 * 1024 * 16);
        server.getConfig().setBufferPoolChunkSize(4096);
        processor.addPlugin(new MonitorFilter(5));
        server.initialize();
    }

}
