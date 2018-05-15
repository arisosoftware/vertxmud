package com.geccocrawler.socks5.handler.ss5;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {
	EventLoopGroup bossGroup;

	private static final Logger logger = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);

	public Socks5CommandRequestHandler(EventLoopGroup bossGroup) {
		this.bossGroup = bossGroup;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg)
			throws Exception {
		logger.info(String.format("Open remote ~ %s://%s:%d", msg.type(), msg.dstAddr(), msg.dstPort()));

		if (msg.type().equals(Socks5CommandType.CONNECT)) {

			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(bossGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

							ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
						}
					});

			ChannelFuture future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
			future.addListener(new ChannelFutureListener() {

				public void operationComplete(final ChannelFuture future) throws Exception {
					if (future.isSuccess()) {

						clientChannelContext.pipeline().addLast(new Client2DestHandler(future));
						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(
								Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
						clientChannelContext.writeAndFlush(commandResponse);
					} else {
						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(
								Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
						clientChannelContext.writeAndFlush(commandResponse);
					}
				}

			});
		} else {
			clientChannelContext.fireChannelRead(msg);
		}
	}

	private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {

		private ChannelHandlerContext clientChannelContext;

		public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
			this.clientChannelContext = clientChannelContext;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
			logger.info("将目标服务器信息转发给客户端");
			clientChannelContext.writeAndFlush(destMsg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
			logger.info("目标服务器断开连接");
			clientChannelContext.channel().close();
		}
	}

 
	private static class Client2DestHandler extends ChannelInboundHandlerAdapter {

		private ChannelFuture destChannelFuture;

		public Client2DestHandler(ChannelFuture destChannelFuture) {
			this.destChannelFuture = destChannelFuture;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			logger.info("将客户端的消息转发给目标服务器端");
			destChannelFuture.channel().writeAndFlush(msg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			logger.info("客户端断开连接");
			destChannelFuture.channel().close();
		}
	}
}
