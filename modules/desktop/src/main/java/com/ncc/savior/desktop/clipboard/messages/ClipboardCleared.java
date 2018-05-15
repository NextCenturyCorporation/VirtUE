package com.ncc.savior.desktop.clipboard.messages;

public class ClipboardCleared extends BaseClipboardMessage {

	public ClipboardCleared(String ownerId) {
		super(ownerId);
	}

	@Override
	public String toString() {
		return "ClipboardCleared [sendTime=" + sendTime + ", clipboardOwnerId=" + clipboardOwnerId + "]";
	}
}
