package io.mio.aio.handler;

import io.mio.aio.support.EventState;
import io.mio.aio.NetFilter;
import io.mio.aio.support.TcpAioSession;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.CompletionHandler;

/**
 * 读写事件回调处理类
 *
 * @author lry
 */
@Slf4j
public class WriteCompletionHandler<T> implements CompletionHandler<Integer, TcpAioSession<T>> {

    @Override
    public void completed(final Integer result, final TcpAioSession<T> aioSession) {
        try {
            NetFilter<T> monitor = aioSession.getServerConfig().getMonitor();
            if (monitor != null) {
                monitor.afterWrite(aioSession, result);
            }
            aioSession.writeToChannel();
        } catch (Exception e) {
            failed(e, aioSession);
        }
    }


    @Override
    public void failed(Throwable exc, TcpAioSession<T> aioSession) {
        try {
            aioSession.getServerConfig().getProcessor().stateEvent(aioSession, EventState.OUTPUT_EXCEPTION, exc);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
        try {
            aioSession.close(true);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

}