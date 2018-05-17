package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public interface IClipboardMessageSenderReceiver {

	/**
	 * Should be called initially and returns the ID
	 *
	 * @return
	 * @throws IOException
	 */
	String init() throws IOException;

	/**
	 * Sends a message back to the hub
	 *
	 * @param message
	 * @throws IOException
	 */
	void sendMessageToHub(IClipboardMessage message) throws IOException;
}
