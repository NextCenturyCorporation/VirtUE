package com.ncc.savior.desktop.clipboard.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.data.PlainTextClipboardData;
import com.ncc.savior.desktop.clipboard.messages.ClientIdClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;

public class ClipboardHub implements IClipboardMessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardHub.class);
	public static final int DEFAULT_PORT = 10022;
	private int i = 0;
	private String hubId = "Hub-0";
	private Map<String, IClipboardMessageSenderReceiver> transmitters;
	private String clipboardOwnerId;

	public ClipboardHub() {
		transmitters = new TreeMap<String, IClipboardMessageSenderReceiver>();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		ServerSocket serverSocket = new ServerSocket(port);
		ClipboardHub hub = new ClipboardHub();
		while (true) {
			Socket socket = serverSocket.accept();
			// BufferedWriter writer = new BufferedWriter(new
			// OutputStreamWriter(socket.getOutputStream()));
			// writer.write("hello??\n");
			// writer.flush();
			// writer.write("helsadlo??\n");
			// writer.flush();
			// writer.write("heldflo??\n");
			// writer.write("helldo??\n");
			// writer.flush();
			// writer.write("helsdfo??\n");
			// writer.write("helasdflo??\n");
			// writer.flush();
			IConnectionWrapper connection = new SocketConnection(socket);
			IMessageSerializer serializer = new JavaObjectMessageSerializer(connection);
			hub.addClient(serializer);
		}
	}

	public void addClient(IMessageSerializer serializer) {
		String newId = getNextId();
		IClipboardMessageSenderReceiver transmitter = new MessageTransmitter(serializer, this, "hub-" + newId);
		ClientIdClipboardMessage idMsg = new ClientIdClipboardMessage(hubId, newId);
		logger.debug("registering new client");
		sendMessageHandleError(idMsg, transmitter, newId);
		transmitters.put(newId, transmitter);
		logger.debug("client added to hub with id=" + newId);
	}

	private synchronized String getNextId() {
		i++;
		return "ClipboardClient-" + i;
	}

	protected static void sslSocketTest() throws IOException, ClassNotFoundException {
		ServerSocketFactory sFactory = SSLServerSocketFactory.getDefault();
		ServerSocket server = sFactory.createServerSocket(1022);
		Socket socket = server.accept();
		logger.debug("got socket");
		// ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		Object o = null;
		try {
			while ((o = in.readObject()) != null) {
				logger.info("read: " + o);
			}
		} catch (EOFException e) {
			// expected
		}

		// ServerSocketListener<Client> ssl = new ServerSocketListener
	}

	@Override
	public void onMessage(IClipboardMessage message) {
		logger.debug("hub received message=" + message);
		if (message instanceof ClipboardChangedMessage) {
			// source has taken control of clipboard
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			this.clipboardOwnerId = m.getSourceId();
			sendMessageToAllButSource(message);
		} else if (message instanceof ClipboardDataRequestMessage) {
			ClipboardDataRequestMessage m = (ClipboardDataRequestMessage) message;
			String destId = this.clipboardOwnerId;
			IClipboardMessageSenderReceiver transmitter = transmitters.get(destId);
			if (transmitter != null) {
				sendMessageHandleError(message, transmitter, destId);
			} else {
				// TODO clipboard cleared?
				transmitter = transmitters.get(message.getSourceId());
				IClipboardMessage dataMessage = new ClipboardDataMessage(hubId, new PlainTextClipboardData(""),
						m.getRequestId(), m.getSourceId());
				sendMessageHandleError(dataMessage, transmitter, message.getSourceId());
			}
		} else if (message instanceof ClipboardDataMessage) {
			ClipboardDataMessage m = (ClipboardDataMessage) message;
			String destId = m.getDestinationId();
			IClipboardMessageSenderReceiver transmitter = transmitters.get(destId);
			if (transmitter != null) {
				sendMessageHandleError(message, transmitter, destId);
			}
		}
	}

	protected void sendMessageToAllButSource(IClipboardMessage message) {
		for (Entry<String, IClipboardMessageSenderReceiver> entry : transmitters.entrySet()) {
			String source = message.getSourceId();
			if (!entry.getKey().equals(source)) {
				IClipboardMessageSenderReceiver transmitter = entry.getValue();
				sendMessageHandleError(message, transmitter, entry.getKey());
			}
		}
	}

	private void sendMessageHandleError(IClipboardMessage message, IClipboardMessageSenderReceiver transmitter,
			String destinationId) {
		try {
			logger.debug("sending message to " + destinationId + " message=" + message);
			transmitter.sendMessageToHub(message);
		} catch (IOException e) {
			onTransmissionError(destinationId, transmitter, message, e);
		}
	}

	private void onTransmissionError(String clientId, IClipboardMessageSenderReceiver transmitter,
			IClipboardMessage message, IOException e) {
		logger.error(
				"Error sending message to client " + clientId + ".  Removing from list of clients.  Message=" + message,
				e);
		transmitters.remove(clientId);
	}

}
