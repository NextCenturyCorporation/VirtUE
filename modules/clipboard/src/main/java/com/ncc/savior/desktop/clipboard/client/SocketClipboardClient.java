package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.net.Socket;

import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;

/**
 * Clipboard client application that uses sockets for the connection. This
 * client is given a hostname and port to connect to where another Socket will
 * be listening.
 * 
 * This class is mainly used for testing.
 */
public class SocketClipboardClient {
	private static final String DEFAULT_HOSTNAME = "localhost";

	public static void main(String[] args) throws IOException, InterruptedException {
		int port = ClipboardHub.DEFAULT_PORT;
		String hostname = DEFAULT_HOSTNAME;
		if (args.length > 0) {
			hostname = args[0];
		}
		if (args.length > 1) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (Exception e) {
				usage("Invalid port: " + args[1]);
			}
		}
		if (args.length > 2) {
			usage("Invalid parameters");
		}
		IClipboardWrapper clipboardWrapper = ClipboardClient.getClipboardWrapperForOperatingSystem(true);
		Socket clientSocket = new Socket(hostname, port);

		IConnectionWrapper connection = new SocketConnection(clientSocket);
		IMessageSerializer serializer = IMessageSerializer.getDefaultSerializer(connection);

		MessageTransmitter transmitter = new MessageTransmitter(serializer, "client");
		transmitter.init();
		ClipboardClient client = new ClipboardClient(transmitter, clipboardWrapper);
		client.waitUntilStopped();
		client.close();
	}

	private static void usage(String string) {
		if (string != null) {
			System.out.println("Error: " + string);
		}
		System.out.println("Usage: executable [hostname [port]]");
		System.out.println("  hostname: optional parameter to set the hostname where the hub is running.  Default: "
				+ DEFAULT_HOSTNAME);
		System.out.println("  port: optional parameter to set the port where the hub is listening.  Default: "
				+ ClipboardHub.DEFAULT_PORT);

	}
}
