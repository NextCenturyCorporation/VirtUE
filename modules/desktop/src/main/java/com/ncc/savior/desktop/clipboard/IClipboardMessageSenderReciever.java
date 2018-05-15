package com.ncc.savior.desktop.clipboard;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public interface IClipboardMessageSenderReciever {

	/**
	 * Sets a handler class to handle income messages from clipboard hub.
	 *
	 * @param iClipboardMessageHandler
	 */
	void setMessageFromHubHandler(IClipboardMessageHandler iClipboardMessageHandler);

	/**
	 * Sends a message back to the hub
	 *
	 * @param message
	 */
	void sendMessageToHub(IClipboardMessage message);
}
