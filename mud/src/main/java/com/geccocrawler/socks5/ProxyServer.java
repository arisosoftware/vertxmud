package com.geccocrawler.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geccocrawler.socks5.handler.ProxyIdleHandler;
import com.geccocrawler.socks5.handler.ss5.Socks5CommandRequestHandler;
import com.geccocrawler.socks5.handler.ss5.Socks5InitialRequestHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class ProxyServer {

	private EventLoopGroup bossGroup = new NioEventLoopGroup();

	public EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);

	private int port;

	private ProxyServer(int port) {
		this.port = port;
	}

	public static ProxyServer create(int port) {
		return new ProxyServer(port);
	}

	public void start() throws Exception {

		EventLoopGroup boss = new NioEventLoopGroup(2);
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

							ch.pipeline().addLast(new IdleStateHandler(3, 30, 0));
							ch.pipeline().addLast(new ProxyIdleHandler());

							ch.pipeline().addLast(Socks5ServerEncoder.DEFAULT);
							ch.pipeline().addLast(new Socks5InitialRequestDecoder());
							ch.pipeline().addLast(new Socks5InitialRequestHandler());

							ch.pipeline().addLast(new Socks5CommandRequestDecoder());
							ch.pipeline().addLast(new Socks5CommandRequestHandler(ProxyServer.this.getBossGroup()));

						}
					});

			ChannelFuture future = bootstrap.bind(port).sync();
			logger.info("proxy bind port : " + port);
			future.channel().closeFuture().sync();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}

}
