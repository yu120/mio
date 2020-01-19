package io.mio.transport.aio;

import io.mio.transport.aio.filter.MonitorFilter;
import io.mio.transport.aio.support.AioMioSession;
import io.mio.transport.aio.support.AioServerConfig;
import io.mio.transport.aio.support.EventState;
import io.mio.transport.aio.support.WriteBuffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DemoServer {

    public static void main(String[] args) throws IOException {
        MessageProcessor<String> messageProcessor = new MessageProcessor<String>() {
            @Override
            public void process0(AioMioSession<String> session, String msg) {
                WriteBuffer outputStream = session.getWriteBuffer();

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
        messageProcessor.addFilter(new MonitorFilter(5));

        AioServerConfig config = new AioServerConfig();
        config.setReadBufferSize(1024 * 1024);
        config.setThreadNum(Runtime.getRuntime().availableProcessors() + 1);
        config.setBufferPoolPageSize(1024 * 1024 * 16);
        config.setBufferPoolChunkSize(4096);
        AioMioServer<String> server = new AioMioServer<>();
        server.initialize(config, new DemoProtocol(), messageProcessor);
    }

}
