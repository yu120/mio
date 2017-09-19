package cn.ms.mio.transport.handler;

import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.mio.transport.MioSession;

public class ReadCompletionHandler<T> implements CompletionHandler<Integer, MioSession<T>> {
    
	private static final Logger logger = LoggerFactory.getLogger(ReadCompletionHandler.class);

    @Override
    public void completed(Integer result, MioSession<T> aioSession) {
        if (result == -1) {
            aioSession.close(false);
            return;
        }
        aioSession.readFromChannel();
    }

    @Override
    public void failed(Throwable exc, MioSession<T> aioSession) {
        logger.error("Read completion handler is failed", exc);
        aioSession.close();
    }
    
}