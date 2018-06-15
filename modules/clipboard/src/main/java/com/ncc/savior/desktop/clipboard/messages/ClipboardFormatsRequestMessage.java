package com.ncc.savior.desktop.clipboard.messages;

public class ClipboardFormatsRequestMessage extends BaseClipboardMessage {

	/**
	 * Requests the current available formats from the clipboard hub. This message
	 * is usually used by a newly connected client to initialize to the state of the
	 * clipboard system.
	 */
	private static final long serialVersionUID = 1L;

	// For Jackson (de)serialization
	protected ClipboardFormatsRequestMessage() {
		this(null);
	}

	public ClipboardFormatsRequestMessage(String messageSourceId) {
		super(messageSourceId);
	}
}
