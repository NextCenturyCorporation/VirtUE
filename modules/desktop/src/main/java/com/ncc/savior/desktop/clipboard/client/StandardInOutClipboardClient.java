package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.StandardInOutConnection;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;

public class StandardInOutClipboardClient {

	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length > 0) {
			usage("No Parameters allowed");
		}
		IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem();

		IConnectionWrapper connection = new StandardInOutConnection();
		IMessageSerializer serializer = new JavaObjectMessageSerializer(connection);

		ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper);
		client.waitUntilStopped();
	}

	private static void usage(String string) {
		if (string != null) {
			System.out.println("Error: " + string);
		}
		System.out.println("Usage: executable");
	}
}
