package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper;
import com.ncc.savior.desktop.clipboard.IClipboardWrapper.IClipboardListener;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.ncc.savior.util.JavaUtil;

/**
 * Clipboard class to be run on a client machine that we want to connect the
 * clipboard to a networked hub.
 *
 *
 */
public class ClipboardClient {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private static final String DEFAULT_HOSTNAME = "localhost";
	private IClipboardMessageSenderReceiver transmitter;
	private IClipboardWrapper clipboard;
	private String myId;

	/**
	 *
	 * @param serializer
	 *            - abstraction for class that can read and write message objects
	 * @param clipboardWrapper
	 *            - abstraction for clipboard functions.
	 */

	public ClipboardClient(IMessageSerializer serializer, IClipboardWrapper clipboardWrapper) throws IOException {
		IClipboardMessageHandler handler = new IClipboardMessageHandler() {
			@Override
			public void onMessage(IClipboardMessage message) {
				onClipboardMessage(message);
			}
		};
		IClipboardMessageSenderReceiver transmitter = new MessageTransmitter(serializer, handler, "client");
		this.myId = transmitter.init();
		logger.debug("new client created with id=" + myId);
		this.transmitter = transmitter;
		this.clipboard = clipboardWrapper;
		IClipboardListener listener = new IClipboardListener() {

			@Override
			public void onPasteAttempt(int format) {
				// clipboard.setDelayedRenderData(new PlaintTextClipboardData("it works"));
				try {
					ClipboardClient.this.transmitter.sendMessageToHub(
							new ClipboardDataRequestMessage(myId, format, UUID.randomUUID().toString()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onClipboardChanged(Set<Integer> formats) {
				try {
					ClipboardClient.this.transmitter.sendMessageToHub(new ClipboardChangedMessage(myId, formats));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		clipboardWrapper.setClipboardListener(listener);
	}

	protected void onClipboardMessage(IClipboardMessage message) {
		logger.debug("got message=" + message);
		if (message instanceof ClipboardChangedMessage) {
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			if (!myId.equals(message.getSourceId())) {
				clipboard.setDelayedRenderFormats(m.getFormats());
			}
		} else if (message instanceof ClipboardDataMessage) {
			clipboard.setDelayedRenderData(((ClipboardDataMessage) message).getData());
		}
	}

	protected static void socketTest() throws IOException, UnknownHostException, InterruptedException {
		SocketFactory socketFactory = SSLSocketFactory.getDefault();
		Socket socket = socketFactory.createSocket("localhost", 1022);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		// ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		for (int i = 0; i < 100; i++) {
			out.writeObject(i + "asfdfa");
			logger.debug("wrote " + i);
		}
		Thread.sleep(3000);
		socket.close();
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		int port = ClipboardHub.DEFAULT_PORT;
		String hostname = DEFAULT_HOSTNAME;
		if (args.length > 1) {
			hostname = args[0];
		}
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		WindowsClipboardWrapper clipboardWrapper = new WindowsClipboardWrapper();
		Socket clientSocket = new Socket(hostname, port);
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(clientSocket.getInputStream()));
		// System.out.println(in.readLine());

		IConnectionWrapper connection = new SocketConnection(clientSocket);
		IMessageSerializer serializer = new JavaObjectMessageSerializer(connection);
		Thread.sleep(1000);

		ClipboardClient client = new ClipboardClient(serializer, clipboardWrapper);
		while (true) {
			// hold
			JavaUtil.sleepAndLogInterruption(1000);
		}
	}
}
