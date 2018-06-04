package io.mio.transport;

import org.micro.URL;

import java.nio.channels.AsynchronousSocketChannel;

public class MioSession<T> {

    public void init(URL url, AsynchronousSocketChannel channel,
                     ReadCompletionHandler<T> readCompletionHandler, WriteCompletionHandler<T> writeCompletionHandler) {

    }
}
