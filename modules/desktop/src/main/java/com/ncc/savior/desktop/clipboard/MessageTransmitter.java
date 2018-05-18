package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.messages.ClientIdClipboardMessage;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.desktop.clipboard.serialization.IMessageSerializer;

public class MessageTransmitter implements IClipboardMessageSenderReceiver {
	private static final Logger logger = LoggerFactory.getLogger(MessageTransmitter.class);

	private IClipboardMessageHandler handler;
	private IMessageSerializer serializer;

	private Thread receiveThread;

	private boolean stopReadThread = false;
	private boolean valid = true;

	private String groupId;

	public MessageTransmitter(IMessageSerializer serializer, IClipboardMessageHandler messageHandler, String threadId) {
		this(null, serializer, messageHandler, threadId);
	}

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

}
