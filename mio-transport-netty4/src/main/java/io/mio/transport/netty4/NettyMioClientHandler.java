package io.mio.transport.netty4;

import io.mio.core.commons.MioCallback;
import io.mio.core.MioConstants;
import io.mio.core.commons.MioMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NettyMioClientHandler
 * <p>
 * Tips：
 * 1.Used to read the information sent by the client
 * 2.ChannelRead0() of SimpleChannelInboundHandler does not need to care about releasing ByteBuf
 *
 * @author lry
 */
@Slf4j
@AllArgsConstructor
@ChannelHandler.Sharable
public class NettyMioClientHandler extends SimpleChannelInboundHandler<MioMessage> {

    private final AttributeKey<MioCallback<MioMessage>> mioCallbackKey;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.debug("Client channel registered:{}", getChannelKey(channel));
        super.channelRegistered(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, final MioMessage msg) throws Exception {
        Channel channel = ctx.channel();
        MioCallback<MioMessage> mioCallback = channel.attr(mioCallbackKey).getAndSet(null);
        mioCallback.notifyListener().onSuccess(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client channel active:{}", getChannelKey(ctx.channel()));
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client channel inactive:{}", getChannelKey(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        MioCallback<MioMessage> mioCallback = channel.attr(mioCallbackKey).getAndSet(null);
        mioCallback.notifyListener().onFailure(cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.debug("Client channel unregistered:{}", getChannelKey(channel));
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
