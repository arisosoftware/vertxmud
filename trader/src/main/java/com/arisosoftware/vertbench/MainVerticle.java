package com.arisosoftware.vertbench;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * An example illustrating how worker verticles can be deployed and how to
 * interact with them.
 *
 * This example prints the name of the current thread at various locations to
 * exhibit the event loop <-> worker thread switches.
 */
public class MainVerticle extends AbstractVerticle {

	int TotalTasks = 50;
	StopWatchInfo sw = new StopWatchInfo();

	@Override
	public void start() throws Exception {
		System.out.println("[Main] Running in " + Thread.currentThread().getName());
		sw.Message = "Main";
		sw.Start();
		EventBus eb = vertx.eventBus();

		for (int questionId = 0; questionId < TotalTasks; questionId++) {

			String message = "hello world #";// + questionId;

			eb.send(BenchApp.Topic, message);

		}

		ReduceHandler reduce = new ReduceHandler();
		reduce.total = TotalTasks;

		eb.consumer(BenchApp.TopicResult, reduce);

	}

	class ReduceHandler implements Handler<Message<Object>> {
		int total;

		@Override
		public void handle(Message<Object> message) {
			total--;
			System.out.println("Receive: " + total + " " + message.body());

			if (total == 0) {
				vertx.eventBus().publish(BenchApp.TopicShutdown, "shutdown");

				// then print all timeinfo
				sw.Stop();
				System.out.println("=================\n\nTotal:" + sw.Report());
				System.exit(0);
			}

		}

	}

}
