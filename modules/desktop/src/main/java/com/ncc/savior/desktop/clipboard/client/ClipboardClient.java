package com.ncc.savior.desktop.clipboard.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
import com.ncc.savior.desktop.clipboard.data.ClipboardData;
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
	private long timeoutMillis = 3000;
	private Map<String, Thread> requestToThread;
	private Map<String, ClipboardData> requestToData;

	/**
	 *
	 * @param serializer
	 *            - abstraction for class that can read and write message objects
	 * @param clipboardWrapper
	 *            - abstraction for clipboard functions.
	 */

	public ClipboardClient(IMessageSerializer serializer, IClipboardWrapper clipboardWrapper) throws IOException {
		this.requestToThread = new TreeMap<String, Thread>();
		this.requestToData = new TreeMap<String, ClipboardData>();
		IClipboardMessageHandler handler = new IClipboardMessageHandler() {
			@Override
			public void onMessage(IClipboardMessage message, String messageHandlerGroupId) {
				onClipboardMessage(message);
			}

			@Override
			public void onMessageError(IOException e) {
				// TODO should we let someone else listen to this event?
				logger.error("Message error.  Client stopped. ", e);

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
				try {
					ClipboardDataRequestMessage requestMsg = new ClipboardDataRequestMessage(myId, format,
							UUID.randomUUID().toString());
					logger.debug("Sending message=" + requestMsg);
					ClipboardClient.this.transmitter.sendMessageToHub(requestMsg);
					ClipboardData clipboardData = blockForClipboardData(requestMsg.getRequestId());
					if (clipboardData != null) {
						clipboard.setDelayedRenderData(clipboardData);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onClipboardChanged(Set<Integer> formats) {
				try {
					ClipboardChangedMessage msg = new ClipboardChangedMessage(myId, formats);
					logger.debug("sending message=" + msg);
					ClipboardClient.this.transmitter.sendMessageToHub(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		clipboardWrapper.setClipboardListener(listener);
	}

	protected ClipboardData blockForClipboardData(String requestId) {
		requestToThread.put(requestId, Thread.currentThread());
		try {
			logger.debug("blocking for data.  request=" + requestId);
			Thread.sleep(timeoutMillis);
		} catch (InterruptedException e) {
			logger.debug("Interrupted!  request=" + requestId);
			ClipboardData data = requestToData.remove(requestId);
			if (data == null) {
				logger.error("Error getting data for request=" + requestId);
			}
			// make sure interupt is cleared
			Thread.interrupted();
			return data;
		} finally {
			// clear the maps always
			requestToThread.remove(requestId);
			requestToData.remove(requestId);
		}
		logger.error("Request for data with id=" + requestId + " timedout!");
		return null;
	}

	protected void onClipboardMessage(IClipboardMessage message) {
		logger.debug("got message=" + message);
		if (message instanceof ClipboardChangedMessage) {
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			if (!myId.equals(message.getSourceId())) {
				clipboard.setDelayedRenderFormats(m.getFormats());
			}
		} else if (message instanceof ClipboardDataMessage) {
			storeClipboardData((ClipboardDataMessage) message);
		} else if (message instanceof ClipboardDataRequestMessage) {
			ClipboardDataRequestMessage m = ((ClipboardDataRequestMessage) message);
			ClipboardData data = clipboard.getClipboardData(m.getFormat());
			ClipboardDataMessage dataMessage = new ClipboardDataMessage(myId, data, m.getRequestId(), m.getSourceId());
			try {
				logger.debug("sending message=" + dataMessage);
				transmitter.sendMessageToHub(dataMessage);
			} catch (IOException e) {
				logger.error("Error sending data message=" + dataMessage);
			}
		}
	}

	/**
	 * Returns true if the internal transmitter is valid. Internal transmitter is
	 * valid as long as it hasn't throw an error on transmission. Tranmission errors
	 * are assumed to be fatal.
	 *
	 * @return
	 */
	public boolean isValid() {
		return transmitter.isValid();
	}

	private void storeClipboardData(ClipboardDataMessage message) {
		String reqId = message.getRequestId();
		Thread t = requestToThread.get(reqId);
		if (t != null) {
			logger.debug("storing data.  request=" + reqId);
			requestToData.put(reqId, message.getData());
			t.interrupt();
		} else {
			logger.error("unable to find thread to interrupt for clipboard data");
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
			if (!client.isValid()) {
				break;
			}
		}
	}
}
