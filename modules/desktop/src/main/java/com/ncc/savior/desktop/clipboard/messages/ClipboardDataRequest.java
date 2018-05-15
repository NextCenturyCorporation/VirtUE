package com.ncc.savior.desktop.clipboard.messages;

public class ClipboardDataRequest extends BaseClipboardMessage {

	private int format;

	public ClipboardDataRequest(String ownerId, int format) {
		super(ownerId);
		this.format = format;
	}

	public int getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return "ClipboardDataRequest [format=" + format + ", sendTime=" + sendTime + ", clipboardOwnerId="
				+ clipboardOwnerId + "]";
	}

}
