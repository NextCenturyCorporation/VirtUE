package com.ncc.savior.desktop.clipboard.messages;

/**
 * This type is used to determine what subsystem (I.E. clipboard, drag and drop)
 * should handle the a certain message. It allows us to use one message stream
 * for multiple sub systems.
 *
 */
public enum MessageType {
	CLIPBOARD, DND

}
