package com.ariso.vertxproxy;

import com.geccocrawler.socks5.ProxyServer;
import com.vertx.mud.ForwardProxyServer;

import io.vertx.core.Vertx;

public class ProxyApp {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Vertx vertx = Vertx.vertx();
		// vertx.deployVerticle(new TelnetServer());
		WrapSocketForwarder w1 = new WrapSocketForwarder();
		w1.setup("9501:127.0.0.1:9502:0:W001");
		vertx.deployVerticle(w1);

		WrapSocketForwarder w2 = new WrapSocketForwarder();
		w2.setup("9502:127.0.0.1:9503:1:W002");
		vertx.deployVerticle(w2);

		ProxyServer.create(9503).start();
	}

}
