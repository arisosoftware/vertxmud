package com.arisosoftware.vertbench;

import com.arisosoftware.vertbench.cpu.Murmur3;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * An example of worker verticle
 */
public class WorkerVerticle extends AbstractVerticle {

	public int WorkerId = 0;

	public int HashResultMask;
	public int HashResultPattern;

	String ResolveTheHashQuestion(StopWatchInfo info, String body) {
		String reply = "";
		// Do the blocking operation in here
		info.Message = Thread.currentThread().getName();
		for (int i = 0; i < 100000000; i++) {
			String Bx = body + i;

			int hash32 = Murmur3.hash32(Bx.getBytes());
			if ((hash32 & HashResultMask) == HashResultPattern) {
				reply = String.format("[%s] + [%d] = [%X]", body, i, hash32);
				break;
			}
		}
		info.Stop();
		reply = String.format("Worker#%d %s  // %s", this.WorkerId, reply, info.Report());
		return reply;
	}

	public boolean SeqOrderFlag = false;

	@Override
	public void start() throws Exception {
		boolean ExecuteBlockingFlag = true;
		vertx.eventBus().<String>consumer(BenchApp.Topic, message -> {
			StopWatchInfo info = new StopWatchInfo();
			String body = message.body();

			// executeBlocking(Handler<Future<T>> blockingCodeHandler, boolean ordered,
			// Handler<AsyncResult<T>> resultHandler)

			Handler<Future<String>> ExecuteHashSoultion = ar -> {
				ar.complete(ResolveTheHashQuestion(info, body));
			};

			Handler<io.vertx.core.AsyncResult<String>> AsyncResp = async -> {
				if (async.succeeded()) {
					vertx.eventBus().send(BenchApp.TopicResult, async.result());
				} else {
					vertx.eventBus().send(BenchApp.TopicResult, async.cause());
				}
			};

			if (ExecuteBlockingFlag) {

				vertx.<String>executeBlocking(ExecuteHashSoultion, SeqOrderFlag, AsyncResp);

			} else {
				vertx.eventBus().send(BenchApp.TopicResult, ResolveTheHashQuestion(info, body));
			}

		});

		vertx.eventBus().<String>consumer(BenchApp.TopicShutdown, message -> {
			vertx.undeploy(this.deploymentID());
			// System.out.println("bye bye. from vertx:" + this.deploymentID() + "
			// WorkerId:" + WorkerId);
		});

	}
}
