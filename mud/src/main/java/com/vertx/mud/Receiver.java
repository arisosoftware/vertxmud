package com.vertx.mud;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Receiver extends AbstractVerticle {

	@Override
	public void start() throws Exception {

		EventBus eb = vertx.eventBus();

		eb.consumer("news-feed", message -> System.out.println("Received news on consumer 1: " + message.body()));

		eb.consumer("news-feed", message -> System.out.println("Received news on consumer 2: " + message.body()));

		eb.consumer("news-feed", message -> System.out.println("Received news on consumer 3: " + message.body()));

		MessageConsumer<Object> msg = eb.consumer("news-feed");

		// 想知道，如何不监听一个eb. 如果verticle被移除，会咋样？
		// msg
		// .handler(message -> {
		// System.out.println(message.body());
		// });

		System.out.println("Ready!");
	}
}
