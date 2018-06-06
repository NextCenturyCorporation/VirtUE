package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.messages.ClientIdClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
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

	private IClipboardMessageHandler handler;
	private IMessageSerializer serializer;

	private Thread receiveThread;

	private boolean stopReadThread = false;
	private boolean valid = true;

	private String groupId;

	/**
	 *
	 * @param serializer
	 * @param messageHandler
	 * @param threadId
	 *            - the ID that should be assigned to the thread created. This is
	 *            mainly for debugging purposes.
	 */
	public MessageTransmitter(IMessageSerializer serializer, IClipboardMessageHandler messageHandler, String threadId) {
		this(null, serializer, messageHandler, threadId);
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
	public MessageTransmitter(String groupId, IMessageSerializer serializer, IClipboardMessageHandler messageHandler,
			String threadId) {
		this.handler = messageHandler;
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
				if (handler != null) {
					handler.onMessage(msg, groupId);
				} else {
					logger.error("message lost due to null handler.  Message=" + msg);
				}
			} catch (IOException e) {
				logger.error("Error trying to deserialize message", e);
				onMessageError(e);
			} catch (Throwable e) {
				logger.error("Unknown error", e);
				onMessageError(new IOException(e));
			}
		}
	}

	private void onMessageError(IOException e) {
		stopReadThread = true;
		handler.onMessageError(e);
		valid = false;
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

		String id = cicm.getNewId();
		receiveThread.setName("MessageReceiver-client-" + id);
		logger.debug("initialized");
		return id;
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

}
