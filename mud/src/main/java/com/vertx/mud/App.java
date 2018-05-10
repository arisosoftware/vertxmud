package com.vertx.mud;

import io.vertx.core.Vertx;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();
	//	vertx.deployVerticle(new TelnetServer());
		ForwardProxyServer proxyserver = new ForwardProxyServer();
		proxyserver.setup("7777:ttc.ca:80");
		vertx.deployVerticle(proxyserver);
		
	}
}
