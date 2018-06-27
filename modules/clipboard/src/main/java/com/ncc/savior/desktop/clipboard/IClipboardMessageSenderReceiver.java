package com.ncc.savior.desktop.clipboard;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public interface IClipboardMessageSenderReceiver extends Closeable {

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

	/**
	 * ID used to determine whether data should pass between the two endpoints.
	 *
	 * @return
	 */
	String getGroupId();

	/**
	 * wait until transmitter is stopped and no longer valid.
	 */
	void waitUntilStopped();

	/**
	 * Gets the messsage passing client id.
	 * 
	 * @return
	 */
	String getClientId();

	/**
	 * Set handler that will only be passed Clipboard messages
	 * 
	 * @param clipboardMessageHandler
	 */
	void setClipboardMessageHandler(IClipboardMessageHandler clipboardMessageHandler);

	/**
	 * set handler that will only be passed Drag and Drop messages.
	 * 
	 * @param dndMessageHandler
	 */
	void setDndMessageHandler(IClipboardMessageHandler dndMessageHandler);
}
