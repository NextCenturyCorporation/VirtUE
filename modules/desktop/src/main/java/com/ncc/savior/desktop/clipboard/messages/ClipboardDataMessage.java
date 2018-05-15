package com.ncc.savior.desktop.clipboard.messages;

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

public class ClipboardDataMessage extends BaseClipboardMessage {

	private ClipboardData data;

	public ClipboardDataMessage(String ownerId, ClipboardData data) {
		super(ownerId);
		this.data = data;
	}

	public ClipboardData getData() {
		return data;
	}

	@Override
	public String toString() {
		return "ClipboardDataMessage [data=" + data + ", sendTime=" + sendTime + ", clipboardOwnerId="
				+ clipboardOwnerId + "]";
	}

}
