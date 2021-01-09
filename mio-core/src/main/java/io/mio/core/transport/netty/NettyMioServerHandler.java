package io.mio.core.transport.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.mio.core.MioConstants;
import io.mio.core.MioMessage;
import io.mio.core.MioProcessor;
import io.mio.core.transport.ServerConfig;
import io.mio.core.utils.ExceptionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * NettyMioServerHandler
 * <p>
 * Tips：
 * 1.Used to get information sent by the client
 * 2.ChannelRead0() of SimpleChannelInboundHandler does not need to care about releasing ByteBuf
 *
 * @author lry
 */
@Slf4j
@Getter
@ChannelHandler.Sharable
public class NettyMioServerHandler extends SimpleChannelInboundHandler<MioMessage> {

    /**
     * The max channel connections number
     */
    private final int maxConnections;
    /**
     * The global processor function
     */
    private final MioProcessor<MioMessage> processor;
    /**
     * The all client channel
     */
    private ConcurrentMap<String, Channel> channels;
    private StandardThreadExecutor threadExecutor;

    public NettyMioServerHandler(ServerConfig serverConfig, MioProcessor<MioMessage> processor) {
        super();
        this.maxConnections = serverConfig.getMaxConnections();
        this.processor = processor;
        this.channels = new ConcurrentHashMap<>(Math.min(serverConfig.getMaxConnections(), 100));
        if (serverConfig.isBizThread()) {
            String threadName = String.format("server-%s:%s", serverConfig.getHostname(), serverConfig.getPort());
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadName).setDaemon(true).build();
            this.threadExecutor = new StandardThreadExecutor(serverConfig.getBizCoreThreads(),
                    serverConfig.getBizMaxThreads(), serverConfig.getBizKeepAliveTime(),
                    TimeUnit.MILLISECONDS, serverConfig.getBizQueueCapacity(), threadFactory);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = getChannelKey(channel);

        int channelSize = channels.size();
        if (channelSize > maxConnections) {
            // Exceeding the maximum number of connections limit, direct close connection
            log.warn("Server connected channel out of limit: limit={}, current={}, channel={}",
                    maxConnections, channelSize, channelKey);
            channel.close();
        } else {
            channels.put(channelKey, channel);
            log.debug("Server channel registered:{}", channelKey);
            super.channelRegistered(ctx);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MioMessage msg) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = getChannelKey(channel);

        try {
            if (threadExecutor == null) {
                processor.onProcessor(channel::writeAndFlush, msg);
            } else {
                threadExecutor.execute(() -> processor.onProcessor(channel::writeAndFlush, msg));
            }
        } catch (Throwable t) {
            if (t instanceof RejectedExecutionException) {
                log.warn("Server processor thread pool[{}] rejected:{}", channelKey, t.getMessage());
                channel.writeAndFlush(new MioMessage(MioMessage.THREAD_POOL_REJECTED, ExceptionUtils.toStack(t)));
            } else {
                log.error("Server processor error:{}", channelKey, t);
                channel.writeAndFlush(new MioMessage(MioMessage.SERVICE_ERROR, ExceptionUtils.toStack(t)));
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Server channel active:{}", getChannelKey(ctx.channel()));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Server channel inactive:{}", getChannelKey(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        processor.onFailure(cause);
        ctx.channel().writeAndFlush(new MioMessage(MioMessage.SERVER_ERROR, ExceptionUtils.toStack(cause)));
        log.error("Server error:{}", getChannelKey(ctx.channel()), cause);
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        String channelKey = getChannelKey(ctx.channel());
        channels.remove(channelKey);
        log.debug("Server channel unregistered:{}", channelKey);
        super.channelUnregistered(ctx);
    }

    /**
     * The get channel key
     * <p>
     * Connection unique identification: remote address + local address
     *
     * @param channel {@link Channel}
     * @return channel key
     */
    private String getChannelKey(Channel channel) {
        return String.format("%s->%s", MioConstants.getSocketAddressKey(channel.localAddress()),
                MioConstants.getSocketAddressKey(channel.remoteAddress()));
    }

}
