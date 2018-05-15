package com.ariso.vertxproxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

public class EchoTelnetServer extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(EchoTelnetServer.class);

	int portNumber = 9901;

	@Override
	public void start() throws Exception {

		NetServer netserver = vertx.createNetServer();

		netserver.connectHandler(new Handler<NetSocket>() {

			public int SessionId = 0;

			@Override
			public void handle(NetSocket netSocket) {

				SessionId++;
				System.out.println(String.format("Welcome, you are %d visitor %d", SessionId, netSocket.hashCode()));

				netSocket.endHandler(t -> {
					netSocket.end();
				});

				netSocket.exceptionHandler(ex -> {
					ex.printStackTrace();
					netSocket.close();
				});

				RecordParser parser = RecordParser.newDelimited("\n", netSocket);

				parser.endHandler(v -> {
					netSocket.close();
				}).exceptionHandler(t -> {
					t.printStackTrace();
					netSocket.close();
				}).handler(buffer -> {
					String name = buffer.toString("UTF-8");
					netSocket.write("Hello " + name + "\n", "UTF-8");
				});

			};

		});

		netserver.listen(portNumber, listenResult -> {
			if (listenResult.succeeded()) {
				logger.info(String.format("Telnet Listen success:%d ", portNumber));

			} else {
				logger.error(String.format("Telnet Listen fail %d, because %s.", portNumber,
						listenResult.cause().getMessage(), listenResult.cause()));
			}
		});

	}
}
