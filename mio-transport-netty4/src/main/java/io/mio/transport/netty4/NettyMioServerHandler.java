package io.mio.transport.netty4;

import io.mio.core.commons.*;
import io.mio.core.MioConstants;
import io.mio.core.transport.ServerConfig;
import io.mio.core.utils.ExceptionUtils;
import io.netty.channel.*;
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
    private final MioProcessor<MioMessage> mioProcessor;
    /**
     * The all client channel
     */
    private ConcurrentMap<String, Channel> channels;
    private StandardThreadExecutor standardThreadExecutor;

    public NettyMioServerHandler(ServerConfig serverConfig, MioProcessor<MioMessage> mioProcessor) {
        super();
        this.maxConnections = serverConfig.getMaxConnections();
        this.mioProcessor = mioProcessor;
        this.channels = new ConcurrentHashMap<>(64);

        if (serverConfig.isBizThread()) {
            String threadName = String.format("server-%s:%s", serverConfig.getHostname(), serverConfig.getPort());
            this.standardThreadExecutor = new StandardThreadExecutor(
                    serverConfig.getBizCoreThreads(), serverConfig.getBizMaxThreads(),
                    serverConfig.getBizKeepAliveTime(), TimeUnit.MILLISECONDS, serverConfig.getBizQueueCapacity(),
                    MioConstants.newThreadFactory(threadName, true));
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String channelKey = getChannelKey(ctx.channel());
        int channelSize = channels.size();
        if (channelSize >= maxConnections) {
            // Exceeding the maximum number of connections limit, direct close connection
            log.warn("Server connected channel out of limit: limit={}, current={}, channel={}",
                    maxConnections, channelSize, channelKey);
            ctx.channel().close();
        } else {
            channels.put(channelKey, ctx.channel());
            log.debug("Server channel registered:{}", channelKey);
            super.channelRegistered(ctx);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MioMessage msg) throws Exception {
        /*
         * Notify results outwards.
         * When there is a write operation, you do not need to release the reference of MSG manually.
         * You need to release the reference of MSG manually when only read operation is available.
         */
        try {
            if (standardThreadExecutor == null) {
                mioProcessor.onProcessor(mioMessage -> ctx.channel().writeAndFlush(mioMessage), msg);
            } else {
                standardThreadExecutor.execute(() -> mioProcessor.onProcessor(
                        mioMessage -> ctx.channel().writeAndFlush(mioMessage), msg));
            }
        } catch (Throwable t) {
            if (t instanceof RejectedExecutionException) {
                log.warn("Thread pool rejected:{}", getChannelKey(ctx.channel()), t);
                ctx.channel().writeAndFlush(new MioMessage(MioMessage.THREAD_POOL_REJECTED, ExceptionUtils.toStack(t)));
            } else {
                log.error("Service error:{}", getChannelKey(ctx.channel()), t);
                ctx.channel().writeAndFlush(new MioMessage(MioMessage.SERVICE_ERROR, ExceptionUtils.toStack(t)));
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
        mioProcessor.onFailure(cause);
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
