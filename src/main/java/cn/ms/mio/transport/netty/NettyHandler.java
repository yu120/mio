package cn.ms.mio.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.ms.mio.transport.Processor;

/**
 * The Netty Handler Adapter(Server/Client).<br>
 * <br>
 * 1.server concurrency access control<br>
 * 2.request to be transferred to server<br>
 * 
 * @author lry
 */
@Sharable
public class NettyHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);
	
	int maxChannel = 0;
	long startupGracefully = 0;
	ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
	
	public NettyHandler(Processor processor, int maxChannel, long startupGracefully) {
		this.maxChannel = maxChannel;
		this.startupGracefully = startupGracefully;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
	
	@Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = getChannelKey(channel);
        logger.debug("The client->server [{}] established connection!!!", channelKey);

        if (channels.size() > maxChannel) {// Exceeding the maximum number of connections, direct close connection
        	logger.warn("NettyServer channelConnected channel size out of limit: limit={} current={}", maxChannel, channels.size());
            channel.close();
        } else {
            channels.put(channelKey, channel);
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    	Channel channel = ctx.channel();
        String channelKey = getChannelKey(channel);
        
        logger.debug("The client->server [{}] disconnected!!!", channelKey);
        
        channels.remove(channelKey);
        ctx.fireChannelUnregistered();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }
    
    /**
     * The close all channel
     */
    public void close() {
        for (Channel ch : channels.values()) {
            try {
                if (ch != null) {
                    ch.close();
                }
            } catch (Exception e) {
            	logger.error("NettyAdapter close channel error: " + getChannelKey(ch), e);
            }
        }
    }
    
    /**
     * remote address + local address, as a unique identifier for the connection
     *
     * @param channel
     * @return
     */
    private String getChannelKey(Channel channel) {
    	InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
    	InetSocketAddress local = (InetSocketAddress) channel.localAddress();
    	
        String key = "";
        if (remote == null || remote.getAddress() == null) {
            key += "null-";
        } else {
            key += remote.getAddress().getHostAddress() + ":" + remote.getPort() + "->";
        }

        if (local == null || local.getAddress() == null) {
            key += "null";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort();
        }

        return key;
    }
    
}