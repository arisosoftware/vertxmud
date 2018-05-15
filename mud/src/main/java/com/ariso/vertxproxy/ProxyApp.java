package com.ariso.vertxproxy;

import com.geccocrawler.socks5.ProxyServer;
import com.vertx.mud.ForwardProxyServer;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ProxyApp {

	private static final Logger logger = LoggerFactory.getLogger(ProxyApp.class);

	
	public static void main(String[] args) throws Exception {
		
		TestPath03();
	}

	static void TestPath01() throws Exception {
		Vertx vertx = Vertx.vertx();
		// vertx.deployVerticle(new TelnetServer());
		WrapSocketForwarder w1 = new WrapSocketForwarder();
		w1.setup("9501:127.0.0.1:9502:1:W001");
		vertx.deployVerticle(w1);

		WrapSocketForwarder w2 = new WrapSocketForwarder();
		w2.setup("9502:127.0.0.1:9503:2:W002");
		vertx.deployVerticle(w2);

		ProxyServer.create(9503).start();
	}

	
	/// this is for verify the tunnel build by two wrapsockets and target to telnet server is working or not
	static void TestPath02() throws Exception {
		Vertx vertx = Vertx.vertx();
		EchoTelnetServer echoserver = new EchoTelnetServer();
		vertx.deployVerticle(echoserver);

		WrapSocketForwarder w1 = new WrapSocketForwarder();
		w1.setup("9501:127.0.0.1:9502:1:W001");
		vertx.deployVerticle(w1);

		WrapSocketForwarder w2 = new WrapSocketForwarder();
		w2.setup("9502:127.0.0.1:9901:2:W002");
		vertx.deployVerticle(w2);

	}
	
	/// this is for verify the tunnel build by two wrapsockets and target to telnet server is working or not
		static void TestPath03() throws Exception {
			Vertx vertx = Vertx.vertx();
			EchoTelnetServer echoserver = new EchoTelnetServer();
			vertx.deployVerticle(echoserver);

			WrapSocketForwarder w1 = new WrapSocketForwarder();
			w1.setup("9501:127.0.0.1:9502:1:W001");
			vertx.deployVerticle(w1);

			WrapSocketForwarder w2 = new WrapSocketForwarder();
			w2.setup("9502:ttc.ca:80:2:W002");
			vertx.deployVerticle(w2);

		}
}
