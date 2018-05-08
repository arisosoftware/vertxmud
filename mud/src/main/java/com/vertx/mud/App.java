package com.vertx.mud;

import io.vertx.core.Vertx;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws InterruptedException {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new TelnetServer());
		vertx.deployVerticle(new ForwardProxyServer());
	}
}
