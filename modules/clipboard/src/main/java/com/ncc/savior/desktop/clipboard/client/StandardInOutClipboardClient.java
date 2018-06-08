package com.ncc.savior.desktop.clipboard.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.StandardInOutConnection;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;

public class StandardInOutClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(StandardInOutClipboardClient.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		try {
			if (args.length > 0) {
				if (args[0].equals("test")) {
					runTest();
				}
				usage("No Parameters allowed");
			}
			logger.debug("Creating clipboard wrapper");
			IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem();
			logger.debug("wrapper created!");

			IConnectionWrapper connection = new StandardInOutConnection();
			IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);
			logger.debug("creating client");
			ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper);
			logger.debug("client created");
			client.waitUntilStopped();
		} catch (Throwable t) {
			logger.error("App stopped by error", t);
		}
	}

	private static void runTest() throws IOException {
		new File("./testTouch.del").createNewFile();
		while (true) {
			logger.debug("started");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			logger.debug("about to read");
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug("reading line: " + line);
				writer.write("from client");
				writer.newLine();
				writer.flush();
			}
		}
	}

	private static void usage(String string) {
		if (string != null) {
			logger.info("Error: " + string);
		}
		logger.info("Usage: executable");
	}
}
