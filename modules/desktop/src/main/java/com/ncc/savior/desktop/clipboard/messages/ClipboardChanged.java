package com.ncc.savior.desktop.clipboard.messages;

import java.util.Collection;

public class ClipboardChanged extends BaseClipboardMessage {
	public ClipboardChanged(String ownerId, Collection<Integer> formats) {
		super(ownerId);
		this.formats = formats;
	}

	private Collection<Integer> formats;

	public Collection<Integer> getFormats() {
		return formats;
	}

	public void setFormats(Collection<Integer> formats) {
		this.formats = formats;
	}

	@Override
	public String toString() {
		return "ClipboardChanged [formats=" + formats + ", sendTime=" + sendTime + ", clipboardOwnerId="
				+ clipboardOwnerId + "]";
	}
}
