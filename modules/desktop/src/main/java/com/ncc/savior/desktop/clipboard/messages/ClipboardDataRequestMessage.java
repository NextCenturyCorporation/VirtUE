package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;

/**
 * Message that requests the current active clipboard data for the given format.
 *
 */
public class ClipboardDataRequestMessage extends BaseClipboardMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private int format;
	private String requestId;

	public ClipboardDataRequestMessage(String ownerId, int format, String requestId) {
		super(ownerId);
		this.format = format;
		this.requestId = requestId;
	}

	public int getFormat() {
		return format;
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		return "ClipboardDataRequestMessage [format=" + format + ", requestId=" + requestId + ", sendTime=" + sendTime
				+ ", messageSourceId=" + messageSourceId + "]";
	}

}
