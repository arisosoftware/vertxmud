package com.vertx.mud;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

public class TelnetServer extends AbstractVerticle {

	static int SessionIdGenerator = 0;

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		NetServer netserver = vertx.createNetServer();

		netserver.connectHandler(new Handler<NetSocket>() {

			public int SessionId;

			@Override
			public void handle(NetSocket netSocket) {
				System.out.println("Incoming connection!");
				SessionIdGenerator++;
				SessionId = SessionIdGenerator;

				netSocket.handler(new Handler<Buffer>() {

					@Override
					public void handle(Buffer inBuffer) {

						// inBuffer keep the incoming data
						// create outBuffer for writing back

						// System.out.println("incoming data: " + inBuffer.length());

						// send message to receiver
						//

						Buffer outBuffer = Buffer.buffer();
						// outBuffer.appendString("response...");

						netSocket.write(outBuffer);
					}

				});

				netSocket.endHandler((v) -> netSocket.end());
				netSocket.exceptionHandler(t -> {
					t.printStackTrace();
					netSocket.close();
				});

				RecordParser parser = RecordParser.newDelimited("\n", netSocket);

				parser.endHandler(v -> netSocket.close()).exceptionHandler(t -> {
					t.printStackTrace();
					netSocket.close();
				}).handler(buffer -> {

					// option 1
					// String data = buffer.getString(0, buffer.length());
					// Buffer outBuffer = Buffer.buffer();
					// outBuffer.appendString("Hello... ").appendString(data);
					// netSocket.write(outBuffer);
					// option 2
					String name = buffer.toString("UTF-8");
					netSocket.write("Hello " + name + "\n", "UTF-8");

				});

			};

		});

		netserver.listen(6666);

	}

}