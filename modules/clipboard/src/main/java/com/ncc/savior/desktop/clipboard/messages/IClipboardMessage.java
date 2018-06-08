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
}
