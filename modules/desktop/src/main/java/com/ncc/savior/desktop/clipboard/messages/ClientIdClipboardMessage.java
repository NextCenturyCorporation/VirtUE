package com.ncc.savior.desktop.clipboard.messages;

/**
 * Message to inform the client of their ID. The client should wait for this on
 * connection and then use the ID in all messages it sends.
 *
 *
 */
public class ClientIdClipboardMessage extends BaseClipboardMessage implements IClipboardMessage {

	private static final long serialVersionUID = 1L;
	private String newId;

	public ClientIdClipboardMessage(String messageSourceId, String newId) {
		super(messageSourceId);
		this.newId = newId;
	}

	public String getNewId() {
		return newId;
	}

	@Override
	public String toString() {
		return "ClientIdClipboardMessage [newId=" + newId + ", sendTime=" + sendTime + ", messageSourceId="
				+ messageSourceId + "]";
	}
}
