package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.messages.ClientIdClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.MessageType;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;

/**
 * Main implementation of {@link IClipboardMessageSenderReceiver} that will
 * handle reading messages from the serializer and sending messages. It also
 * helps with initialization and maintaining whether the connection is valid or
 * what group it is associated with.
 *
 * Clients need to make sure they call the {@link #init()} method once they are
 * connected to receive an ID.
 */
public class MessageTransmitter implements IClipboardMessageSenderReceiver {
	private static final Logger logger = LoggerFactory.getLogger(MessageTransmitter.class);

	private IClipboardMessageHandler clipboardMessageHandler;
	private IClipboardMessageHandler dndMessageHandler;
	private IMessageSerializer serializer;

	private Thread receiveThread;

	private boolean stopReadThread = false;
	private boolean valid = true;

	private String groupId;

	private String clientId;

	/**
	 *
	 * @param serializer
	 * @param messageHandler
	 * @param threadId
	 *            - the ID that should be assigned to the thread created. This is
	 *            mainly for debugging purposes.
	 */
	public MessageTransmitter(IMessageSerializer serializer, String threadId) {
		this(null, serializer, threadId);
	}

	/**
	 *
	 * @param groupId
	 *            - ID used to determine if data should flow between 2 different
	 *            clients.
	 * @param serializer
	 * @param messageHandler
	 * @param threadId
	 *            - the ID that should be assigned to the thread created. This is
	 *            mainly for debugging purposes.
	 */
	public MessageTransmitter(String groupId, IMessageSerializer serializer, String threadId) {
		this.serializer = serializer;
		this.groupId = groupId;
		this.receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				readMessagesForever();

			}
		}, "MessageReceiver-" + threadId);
		this.receiveThread.setDaemon(true);
		this.receiveThread.start();
	}

	protected void readMessagesForever() {
		while (!stopReadThread) {
			try {
				IClipboardMessage msg = serializer.deserialize();
				logger.debug("got message " + msg);
				MessageType type = msg.getType();
				IClipboardMessageHandler handler = null;
				switch (type) {
				case CLIPBOARD:
					handler = clipboardMessageHandler;
					break;
				case DND:
					handler = dndMessageHandler;
					break;
				}
				if (handler != null) {
					handler.onMessage(msg, groupId);
				} else {
					logger.error("message lost due to null handler.  Message=" + msg);
				}
			} catch (IOException e) {
				onMessageError("Error trying to deserialize message", e);
			} catch (Throwable e) {
				onMessageError("Unknown error", new IOException(e));
			}
		}
	}

	private void onMessageError(String description, IOException e) {
		if (!stopReadThread) {
			clipboardMessageHandler.onMessageError(description, e);
			try {
				close();
			} catch (IOException ioe) {
				logger.warn("error closing " + this + ".", ioe);
			}
		}
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public void sendMessageToHub(IClipboardMessage message) throws IOException {
		try {
			logger.debug("sending message " + message);
			serializer.serialize(message);
		} catch (IOException e) {
			valid = false;
			throw e;
		}
	}

	/**
	 * Needs to be called for clients. This blocks and waits for a
	 * {@link ClientIdClipboardMessage} which starts the connection.
	 */
	@Override
	public String init() throws IOException {
		IClipboardMessage msg = null;
		while (!((msg = serializer.deserialize()) instanceof ClientIdClipboardMessage)) {
			logger.warn("skipping message until Id received.  Message=" + msg);
		}
		ClientIdClipboardMessage cicm = (ClientIdClipboardMessage) msg;

		clientId = cicm.getNewId();
		receiveThread.setName("MessageReceiver-client-" + clientId);
		logger.debug("initialized");
		return clientId;
	}

	/**
	 * Waits until message receiver thread has stopped. At that point, the
	 * transmitter should not be used anymore.
	 */
	@Override
	public void waitUntilStopped() {
		try {
			receiveThread.join();
		} catch (InterruptedException e) {
			logger.warn("Waiting thread was interrupted!", e);
		}
	}

	@Override
	public void close() throws IOException {
		stopReadThread = true;
		valid = false;
		serializer.close();
		serializer = new IMessageSerializer() {

			@Override
			public void close() throws IOException {
				// do nothing
			}

			@Override
			public void serialize(IClipboardMessage message) throws IOException {
				throw new RuntimeException("MessageTransmitter has been closed!");
			}

			@Override
			public IClipboardMessage deserialize() throws IOException {
				throw new RuntimeException("MessageTransmitter has been closed!");
			}
		};
		if (clipboardMessageHandler != null) {
			clipboardMessageHandler.closed();
		}
		if (dndMessageHandler != null) {
			dndMessageHandler.closed();
		}
	}

	@Override
	public void setClipboardMessageHandler(IClipboardMessageHandler clipboardMessageHandler) {
		this.clipboardMessageHandler = clipboardMessageHandler;
	}

	@Override
	public void setDndMessageHandler(IClipboardMessageHandler dndMessageHandler) {
		this.dndMessageHandler = dndMessageHandler;
	}

	@Override
	public String getClientId() {
		return clientId;
	}
}
