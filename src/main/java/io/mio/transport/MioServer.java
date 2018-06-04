package io.mio.transport;

import lombok.extern.slf4j.Slf4j;
import org.micro.URL;
import org.micro.thread.NamedThreadFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;

/**
 * The Micro Service Server IO
 *
 * @author lry
 */
@Slf4j
public class MioServer<T> {

    private AsynchronousChannelGroup asyncChannelGroup;
    private AsynchronousServerSocketChannel asyncServerChannel;

    public final void start(final URL url) throws IOException {
        log.info("The mio server config is {}", url);

        try {
            this.asyncChannelGroup = this.buildAsyncChannelGroup(url);
            this.asyncServerChannel = this.buildAsyncServerChannel(url, asyncChannelGroup);

            asyncServerChannel.accept(asyncServerChannel, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                @Override
                public void completed(final AsynchronousSocketChannel channel, AsynchronousServerSocketChannel serverSocketChannel) {
                    serverSocketChannel.accept(serverSocketChannel, this);
                    buildMioSession(channel, url);
                }

                @Override
                public void failed(Throwable exc, AsynchronousServerSocketChannel serverSocketChannel) {
                    log.error("smart-socket server accept fail", exc);
                }
            });
        } catch (IOException e) {
            shutdown();
            throw e;
        }
    }

    public void shutdown() {
        try {
            if (asyncServerChannel != null) {
                asyncServerChannel.close();
                asyncServerChannel = null;
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        if (asyncChannelGroup != null) {
            asyncChannelGroup.shutdown();
            asyncChannelGroup = null;
        }
    }

    private AsynchronousChannelGroup buildAsyncChannelGroup(URL url) throws IOException {
        int threadNum = url.getParameter("threadNum", Runtime.getRuntime().availableProcessors() + 1);
        ThreadEnum threadEnum = url.getParameter("threadType", ThreadEnum.FIXED);
        if (ThreadEnum.FIXED == threadEnum) {
            return AsynchronousChannelGroup.withFixedThreadPool(threadNum, new NamedThreadFactory("mio"));
        } else if (ThreadEnum.CACHED == threadEnum) {
            return AsynchronousChannelGroup.withFixedThreadPool(threadNum, new NamedThreadFactory("mio"));
        } else {
            throw new IllegalArgumentException("The illegal thread type: " + threadEnum);
        }
    }

    private AsynchronousServerSocketChannel buildAsyncServerChannel(URL url, AsynchronousChannelGroup asyncChannelGroup) throws IOException {
        AsynchronousServerSocketChannel asyncServerChannel = AsynchronousServerSocketChannel.open(asyncChannelGroup);

        //set socket options
        for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
            if (entry.getKey().startsWith("socket-option.")) {
                //asyncServerChannel.setOption(null, entry.getValue());
            }
        }

        //bind host
        if (url.getHost() != null) {
            asyncServerChannel.bind(new InetSocketAddress(url.getHost(), url.getPort()), 1000);
        } else {
            asyncServerChannel.bind(new InetSocketAddress(url.getPort()), 1000);
        }

        return asyncServerChannel;
    }

    private void buildMioSession(AsynchronousSocketChannel socketChannel, URL url) {
        MioSession<T> mioSession = new MioSession<>();
        mioSession.init(url, socketChannel, new ReadCompletionHandler<T>(), new WriteCompletionHandler<T>());
    }

}
