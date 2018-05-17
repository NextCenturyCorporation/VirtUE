package com.ncc.savior.desktop.clipboard.messages;

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
