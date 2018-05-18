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

	/**
	 * Returns true as long as the instance hasn't throw an error on transmission.
	 * Transmission errors are assumed to be fatal.
	 *
	 * @return
	 */
	public boolean isValid();

	String getGroupId();
}
