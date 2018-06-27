package com.ncc.savior.desktop.clipboard.messages;

import java.util.Date;

/**
 * Interface for all clipboard messages passed between clients and the hub. This
 * represents the protocol for the clipboard system.
 *
 *
 */
public interface IClipboardMessage {
	/**
	 * Returns the time the message was created and sent.
	 * 
	 * @return
	 */
	Date getSendTime();

	/**
	 * Returns the String ID of the client that sent the message.
	 * 
	 * @return
	 */
	String getSourceId();

	/**
	 * Get the message type which is used to determine what subsystem (I.E.
	 * clipboard, drag and drop) should handle the message.
	 * 
	 * @return
	 */
	MessageType getType();
}
