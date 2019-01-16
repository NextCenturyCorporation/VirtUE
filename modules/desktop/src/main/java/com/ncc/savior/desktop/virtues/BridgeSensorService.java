package com.ncc.savior.desktop.virtues;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BridgeSensorService {
	private static final Logger logger = LoggerFactory.getLogger(BridgeSensorService.class);

	private ObjectMapper jsonMapper;
	private BlockingQueue<BridgeSensorMessage> messageQueue;
	private BufferedOutputStream os;
	private Socket socket;
	private boolean enabled;

	public BridgeSensorService(long timeoutMillis, int port, String host, boolean enabled)
			throws UnknownHostException, IOException {
		this.enabled = enabled;
		if (enabled) {
			this.messageQueue = new LinkedBlockingQueue<BridgeSensorMessage>();
			JsonFactory jf = new JsonFactory();
			jf.disable(Feature.AUTO_CLOSE_TARGET);
			this.jsonMapper = new ObjectMapper(jf);
			this.socket = new Socket(host, port);
			this.os = new BufferedOutputStream(socket.getOutputStream());

			String newLine = "\n";
			byte[] newLineBytes = newLine.getBytes();

			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						try {
							BridgeSensorMessage messageObj = messageQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
							if (messageObj != null) {
								if (socket.isClosed()) {
									socket = new Socket(host, port);
									os = new BufferedOutputStream(socket.getOutputStream());
								}

								jsonMapper.writeValue(os, messageObj);
								os.write(newLineBytes);
								os.flush();
							} else {
								os.close();
							}
						} catch (Exception e) {
							logger.error("Error with sending JSON data to bridge sensor");
						}
					}
				}

			}, "Message Thread");
			thread.start();
		}
	}

	public void sendMessage(BridgeSensorMessage messageObj) {
		if (enabled) {
			messageQueue.add(messageObj);
		}
	}

}
