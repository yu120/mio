package cn.ms.mio.transport.handler;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.AbstractMap.SimpleEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.mio.transport.MioSession;

public class WriteCompletionHandler<T> implements CompletionHandler<Integer, SimpleEntry<MioSession<T>, ByteBuffer>> {
    
	private static final Logger logger = LoggerFactory.getLogger(WriteCompletionHandler.class);

    @Override
    public void completed(Integer result, SimpleEntry<MioSession<T>, ByteBuffer> attachment) {
        MioSession<T> aioSession = attachment.getKey();
        ByteBuffer writeBuffer = attachment.getValue();
        //服务端Session才具备流控功能
        aioSession.tryReleaseFlowLimit();
        aioSession.writeToChannel(writeBuffer.hasRemaining() ? writeBuffer : null);
    }

    @Override
    public void failed(Throwable exc, SimpleEntry<MioSession<T>, ByteBuffer> attachment) {
        logger.warn(exc.getMessage());
        attachment.getKey().close();
    }
    
}