package com.geccocrawler.socks5.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ProxyChannelTrafficShapingHandler extends ChannelTrafficShapingHandler {

	public static final String PROXY_TRAFFIC = "ProxyChannelTrafficShapingHandler";

	private long beginTime;

	private long endTime;

	 
	private static final Logger logger = LoggerFactory.getLogger(ProxyChannelTrafficShapingHandler.class);

	private ChannelListener channelListener;

	public static ProxyChannelTrafficShapingHandler get(ChannelHandlerContext ctx) {
		return (ProxyChannelTrafficShapingHandler) ctx.pipeline().get(PROXY_TRAFFIC);
	}

	public ProxyChannelTrafficShapingHandler(long checkInterval, ChannelListener channelListener) {
		super(checkInterval);

		this.channelListener = channelListener;
	}

	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		beginTime = System.currentTimeMillis();
		if (channelListener != null) {
			channelListener.active(ctx);
		}
		super.channelActive(ctx);
	}

	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		endTime = System.currentTimeMillis();
		if (channelListener != null) {
			channelListener.inActive(ctx);
		}
		logger.debug(ctx);
		super.channelInactive(ctx);
	}

	public long getBeginTime() {
		return beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

 

}
