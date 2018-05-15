package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;

/**
 * Message that requests the current active clipboard data for the given format.
 *
 *
 */
public class ClipboardDataRequestMessage extends BaseClipboardMessage implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int format;

	public ClipboardDataRequestMessage(String ownerId, int format) {
		super(ownerId);
		this.format = format;
	}

	public int getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return "ClipboardDataRequest [format=" + format + ", sendTime=" + sendTime + ", clipboardOwnerId="
				+ messageSourceId + "]";
	}

}
