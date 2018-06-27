package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.StandardInOutConnection;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.dnd.DndBackdrop;

/**
 * Client application that uses standard input and standard output to send
 * clipboard messages. Care must be taken to ensure no classes read System.in or
 * write to System.out, including loggers.
 * 
 * This class is mainly used with SSH connections to remote machines.
 * 
 *
 */
public class StandardInOutClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(StandardInOutClipboardClient.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		DndBackdrop.main(args);
		try {
			if (args.length > 0) {
				if (args[0].equals("test")) {
					runTest();
					System.exit(0);
				}
				usage("No Parameters allowed");
			}
			IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(true);

			IConnectionWrapper connection = new StandardInOutConnection();
			IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
			IClipboardMessageSenderReceiver transmitter = new MessageTransmitter(serializer, "client");
			String id = transmitter.init();
			logger.debug("new client created with id=" + id);

			ClipboardClient client = new ClipboardClient(transmitter, clipboardWrapper);
			client.waitUntilStopped();
			client.close();

		} catch (Throwable t) {
			logger.error("App stopped by error", t);
		}
		System.exit(0);
	}

	private static void runTest() {
		logger.debug("Attempted to run test, but no test configured.  Exiting...");

	}

	private static void usage(String string) {
		if (string != null) {
			logger.info("Error: " + string);
		}
		logger.info("Usage: executable");
	}
}
