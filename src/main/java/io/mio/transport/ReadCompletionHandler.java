package io.mio.transport;

import java.nio.channels.CompletionHandler;

public class ReadCompletionHandler<T> implements CompletionHandler<Integer, MioSession<T>> {

    @Override
    public void completed(Integer result, MioSession<T> attachment) {

    }

    @Override
    public void failed(Throwable exc, MioSession<T> attachment) {

    }

}
