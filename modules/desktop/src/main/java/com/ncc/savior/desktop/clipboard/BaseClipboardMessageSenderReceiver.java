package com.ncc.savior.desktop.clipboard;

/**
 * Abstract class that handles message passing between the Hub and the client.
 * Specific implementations will handle how the data is passed (java objects,
 * JSON, CSV, etc) and what the connection and transport mechanism is (SSH,
 * sockets, ssl sockets).
 *
 *
 */
public abstract class BaseClipboardMessageSenderReceiver implements IClipboardMessageSenderReceiver {

	protected IClipboardMessageHandler clipboardMessageHandler;

	/**
	 * Sets the handler that will be called when a message is received by this class.
	 */
	@Override
	public void setMessageFromHubHandler(IClipboardMessageHandler clipboardMessageHandler) {
		this.clipboardMessageHandler = clipboardMessageHandler;
	}

}
