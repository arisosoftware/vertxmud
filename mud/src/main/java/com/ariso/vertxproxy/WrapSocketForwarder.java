package com.ariso.vertxproxy;

import java.util.HashMap;

import com.vertx.mud.ForwardProxyServer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class WrapSocketForwarder extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(WrapSocketForwarder.class);

	private int port = 3306;
	private String hostname = "10.10.0.6";
	NetServer netServer;

	int LocalPort;

	int EncryptModel = 0;

	// forward format: localport:hostname:port:encryptModel : 6666:remoteSocket:80:0
	// 9501:127.0.0.1:9502:0 ~ 0 for encrypt 9502 is wrapper server port
	// 9502:127.0.0.1:9503:1 ~ 1 for decrypt, 9503 is socket 5 proxy port
	public void setup(String forwardTo) throws Exception {
		String[] arr = forwardTo.split(":");
		LocalPort = Integer.parseInt(arr[0]);
		port = Integer.parseInt(arr[2]);
		hostname = arr[1];
		EncryptModel = Integer.parseInt(arr[3]);
	}

	@Override
	public void start() throws Exception {
		netServer = vertx.createNetServer();// 创建代理服务器

		Encryptor crypt = new Encryptor("111111");

		netServer.connectHandler(incomingSocket -> {
			logger.info("accept new incoming socket");

			NetClient netClient = vertx.createNetClient();
			// connect to outgoing socket
			netClient.connect(port, hostname, result -> {
				if (result.succeeded()) {

					NetSocket outgoingSocket = result.result();

					incomingSocket.handler(buff -> {
						Buffer bf = buff;
						logger.info(String.format("sent C-S %d", bf.length()));
						try {
							crypt.ConvertToEncrpyt(bf);
							outgoingSocket.write(bf);
						} catch (Exception e) {
							e.printStackTrace();
							outgoingSocket.close();
							incomingSocket.close();
						}
					});

					outgoingSocket.handler(buff -> {
						Buffer bf = buff;
						logger.info(String.format("sent S-C %d", bf.length()));

						try {
							crypt.ConvertToDecrpyt(bf);
							incomingSocket.write(bf);
						} catch (Exception e) {
							e.printStackTrace();
							outgoingSocket.close();
							incomingSocket.close();
						}

					});

					incomingSocket.closeHandler(handler -> {
						outgoingSocket.close();
						incomingSocket.close();
					});

					outgoingSocket.closeHandler(handler -> {
						outgoingSocket.close();
						incomingSocket.close();
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
