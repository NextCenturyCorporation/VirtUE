package com.ncc.savior.desktop.clipboard.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageHandler;
import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.MessageTransmitter;
import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.connection.SocketConnection;
import com.ncc.savior.desktop.clipboard.data.EmptyClipboardData;
import com.ncc.savior.desktop.clipboard.guard.ConstantDataGuard;
import com.ncc.savior.desktop.clipboard.guard.ICrossGroupDataGuard;
import com.ncc.savior.desktop.clipboard.messages.ClientIdClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardChangedMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataMessage;
import com.ncc.savior.desktop.clipboard.messages.ClipboardDataRequestMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;
import com.ncc.savior.desktop.clipboard.serialization.JavaObjectMessageSerializer;
import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;

public class ClipboardHub implements IClipboardMessageHandler {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardHub.class);
	public static final int DEFAULT_PORT = 10022;
	private int i = 0;
	private String hubId = "Hub-0";
	private Map<String, IClipboardMessageSenderReceiver> transmitters;
	private String clipboardOwnerId;
	private Collection<Integer> validFormats;
	private ICrossGroupDataGuard dataGuard;

	public ClipboardHub(ICrossGroupDataGuard dataGuard) {
		transmitters = new TreeMap<String, IClipboardMessageSenderReceiver>();
		validFormats = new TreeSet<Integer>();
		validFormats.add(IWindowsClipboardUser32.CF_TEXT);
		validFormats.add(IWindowsClipboardUser32.CF_UNICODE);
		this.dataGuard = dataGuard;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		ServerSocket serverSocket = new ServerSocket(port);
		ClipboardHub hub = new ClipboardHub(new ConstantDataGuard(false));
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
			String defaultGroup = "default";
			hub.addClient(defaultGroup, serializer);
		}
	}

	public void addClient(String groupId, IMessageSerializer serializer) {
		String newId = getNextId();
		// Not entirely sure where to store and map groupId. I'm not sure it belongs in
		// MessageTransmitter, but I do need to be able to get the groupID of a message
		// transmitter (in the HUB, but not in the clients). Creating a separate Map
		// object seems like overkill and could easily cause issues keeping the Maps in
		// sync. Wrapping the transmitters seems more clunky than useful
		IClipboardMessageSenderReceiver transmitter = new MessageTransmitter(groupId, serializer, this, "hub-" + newId);
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
	public void onMessage(IClipboardMessage message, String messageSourceGroupId) {
		logger.debug("hub received message=" + message);
		if (message instanceof ClipboardChangedMessage) {
			// source has taken control of clipboard
			ClipboardChangedMessage m = (ClipboardChangedMessage) message;
			Collection<Integer> formats = m.getFormats();
			filterToValidFormats(formats);
			this.clipboardOwnerId = m.getSourceId();
			sendMessageToAllButSource(message);
		} else if (message instanceof ClipboardDataRequestMessage) {
			ClipboardDataRequestMessage m = (ClipboardDataRequestMessage) message;
			String destId = this.clipboardOwnerId;
			IClipboardMessageSenderReceiver transmitter = transmitters.get(destId);
			boolean allowTransfer = transmitter != null && transmitter.isValid();

			if (allowTransfer) {
				// we have a transmitter and it is ready to go. Proceed to the next step in
				// determining if we should transfer

				// Request for data is going to transmitter.getGroupId(), so that will be the
				// data source
				String dataSourceGroupId = transmitter.getGroupId();
				// Request for data came from messageSourceGroupId, so that will be the
				// destination of the data
				String dataDestinationGroupId = messageSourceGroupId;
				allowTransfer = dataGuard.allowDataTransfer(dataSourceGroupId, dataDestinationGroupId);
			} else {
				// TODO should we do something since this source is no longer connected?
			}
			if (allowTransfer) {
				sendMessageHandleError(message, transmitter, destId);
			} else {
				transmitter = transmitters.get(message.getSourceId());
				int format = ((ClipboardDataRequestMessage) message).getFormat();
				IClipboardMessage dataMessage = new ClipboardDataMessage(hubId, new EmptyClipboardData(format),
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

	private void filterToValidFormats(Collection<Integer> formats) {
		Iterator<Integer> itr = formats.iterator();
		while (itr.hasNext()) {
			Integer format = itr.next();
			if (!validFormats.contains(format)) {
				itr.remove();
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
			if (transmitter.isValid()) {
				transmitter.sendMessageToHub(message);
			} else {
				onTransmissionError(destinationId, transmitter, message, null);
			}
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

	@Override
	public void onMessageError(IOException e) {
		logger.error("message error:", e);
		Iterator<Entry<String, IClipboardMessageSenderReceiver>> itr = transmitters.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, IClipboardMessageSenderReceiver> entry = itr.next();
			if (!entry.getValue().isValid()) {
				itr.remove();
			}
		}
	}

}