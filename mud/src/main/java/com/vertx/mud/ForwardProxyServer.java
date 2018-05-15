package com.vertx.mud;

import java.util.HashMap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class ForwardProxyServer extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(ForwardProxyServer.class);

	private int port = 3306;
	private String hostname = "10.10.0.6";
	NetServer netServer;
	EventBus eventBus;
	HashMap<String, SocketBag> hashMap;

	class SocketBag {
		public NetSocket incomingSocket;
		public NetSocket outgoingSocket;
		public int ID;

		private void close() {
			incomingSocket.close();
			outgoingSocket.close();
		}
	}

	int SID;
	int LocalPort;

	// forward format: localport:hostname:port : 6666:ttc.ca:80
	public void setup(String forwardTo) throws Exception {
		String[] arr = forwardTo.split(":");
		LocalPort = Integer.parseInt(arr[0]);
		port = Integer.parseInt(arr[2]);
		hostname = arr[1];

	}

	@Override
	public void start() throws Exception {
		netServer = vertx.createNetServer();// 创建代理服务器

		SID = 0;
		eventBus = vertx.eventBus();
		hashMap = new HashMap<>();

		netServer.connectHandler(incomingSocket -> {
			logger.info(" incoming socket");

			NetClient netClient = vertx.createNetClient();
			netClient.connect(port, hostname, result -> {
				if (result.succeeded()) {
					SID++;
					SocketBag sb = new SocketBag();
					String Key = "K" + SID;

					String KeyCtoS = Key + "CS";
					String KeyStoC = Key + "SC";

					hashMap.put(Key, sb);
					sb.incomingSocket = incomingSocket;
					sb.outgoingSocket = result.result();

					eventBus.consumer(KeyCtoS, msg -> {
						Buffer bf = (Buffer) msg.body();
						logger.info(String.format("get %s %d", KeyCtoS, bf.length()));
						sb.outgoingSocket.write(bf);
					});

					eventBus.consumer(KeyStoC, msg -> {
						Buffer bf = (Buffer) msg.body();
						logger.info(String.format("get %s %d", KeyStoC, bf.length()));
						sb.incomingSocket.write(bf);
					});

					sb.incomingSocket.handler(buff -> {
						Buffer bf = buff;
						logger.info(String.format("sent %s %d", KeyCtoS, bf.length()));
						eventBus.send(KeyCtoS, bf);

					});

					sb.outgoingSocket.handler(buff -> {
						Buffer bf = buff;
						logger.info(String.format("sent %s %d", KeyStoC, bf.length()));
						eventBus.send(KeyStoC, bf);
					});
				}
			});

		});

		netServer.listen(LocalPort, listenResult -> {
			if (listenResult.succeeded()) {
				logger.info(" proxy server start up.");

			} else {
				logger.error(" proxy exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
				netServer = null;
			}
		});

	}
}
