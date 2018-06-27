package com.ncc.savior.desktop;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.StandardInOutConnection;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.dnd.DndBackdrop;

public class RemoteServiceApplication {
	private static final Logger logger = LoggerFactory.getLogger(RemoteServiceApplication.class);

	public static void main(String[] args) throws IOException, InterruptedException {

		try {
			if (args.length > 0) {
				usage("No Parameters allowed");
			} else {

				IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(true);

				IConnectionWrapper connection = new StandardInOutConnection();
				IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
				MessageTransmitter transmitter = new MessageTransmitter(serializer, "client");
				transmitter.init();
				DndBackdrop dnd = new DndBackdrop(transmitter);
				dnd.setVisible(true);
				ClipboardClient client = new ClipboardClient(transmitter, clipboardWrapper);
				client.waitUntilStopped();
				client.close();
			}
		} catch (Throwable t) {
			logger.error("App stopped by error", t);
		}
		System.exit(0);
	}

	private static void usage(String string) {
		if (string != null) {
			logger.info("Error: " + string);
		}
		logger.info("Usage: executable");
	}
}
