package com.ariso.vertxproxy;

import java.util.HashMap;

import org.apache.sshd.common.util.Base64;

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

	int EncryptModel = 0; // 0 : no encrypt, 1 encrypt then decrypt , 2: decrypt then encrypt

	String ID;

	// forward format: localport:hostname:port:encryptModel : 6666:remoteSocket:80:0
	// 9501:127.0.0.1:9502:0 ~ 0 for encrypt 9502 is wrapper server port
	// 9502:127.0.0.1:9503:1 ~ 1 for decrypt, 9503 is socket 5 proxy port
	public void setup(String forwardTo) throws Exception {
		String[] arr = forwardTo.split(":");
		LocalPort = Integer.parseInt(arr[0]);
		port = Integer.parseInt(arr[2]);
		hostname = arr[1];
		EncryptModel = Integer.parseInt(arr[3]);
		ID = arr[4];
	}

	String PrintSetupInfo()
	{
		return String.format("Listen:%d target:%s:%d Encrypt:%d ID:%s", 
				LocalPort, hostname, port, EncryptModel, ID
				
				);
	}
	
	@Override
	public void start() throws Exception {
		netServer = vertx.createNetServer(); 
		
		Encryptor crypt = new Encryptor("111111");

		netServer.connectHandler(incomingSocket -> {
			logger.info(String.format("%s incoming ", ID));

			NetClient netClient = vertx.createNetClient();
			// connect to outgoing socket
			netClient.connect(port, hostname, result -> {
				if (result.succeeded()) {

					NetSocket outgoingSocket = result.result();

					incomingSocket.handler(bf -> {
					 
						logger.info(String.format("%s sent C-S %d", ID, bf.length()));
						try {
						  if ( this.EncryptModel==1)
							bf = crypt.ConvertToEncrpyt(bf);
						  else if (this.EncryptModel==2)
							  bf = crypt.ConvertToDecrpyt(bf);
					
						  logger.info(Base64.encodeToString(bf.getBytes()));
						  outgoingSocket.write(bf);
						} catch (Exception e) {
							e.printStackTrace();
							outgoingSocket.close();
							incomingSocket.close();
						}
					});

					outgoingSocket.handler(bf -> {
					 
						logger.info(String.format("%s sent S-C %d", ID, bf.length()));

						try {
							
							if ( this.EncryptModel==2)
								bf = crypt.ConvertToEncrpyt(bf);
							  else if (this.EncryptModel==1)
								  bf = crypt.ConvertToDecrpyt(bf);
							logger.info(Base64.encodeToString(bf.getBytes()));
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
				logger.info( PrintSetupInfo()+" Start up!");

			} else {
				logger.error( PrintSetupInfo() + " proxy exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
				netServer = null;
			}
		});

	}

}
