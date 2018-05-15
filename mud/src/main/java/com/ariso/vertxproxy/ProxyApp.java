package com.ariso.vertxproxy;

import com.geccocrawler.socks5.ProxyServer;
import com.vertx.mud.ForwardProxyServer;

import io.vertx.core.Vertx;

public class ProxyApp {

	public static void main(String[] args) throws Exception {
		TestPath02();
	}

	static void TestPath01() throws Exception {
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

	static void TestPath02() throws Exception {
		Vertx vertx = Vertx.vertx();
		EchoTelnetServer echoserver = new EchoTelnetServer();
		vertx.deployVerticle(echoserver);

		WrapSocketForwarder w1 = new WrapSocketForwarder();
		w1.setup("9501:127.0.0.1:9502:0:W001");
		vertx.deployVerticle(w1);

		WrapSocketForwarder w2 = new WrapSocketForwarder();
		w2.setup("9502:127.0.0.1:9901:1:W002");
		vertx.deployVerticle(w2);

	}
}
