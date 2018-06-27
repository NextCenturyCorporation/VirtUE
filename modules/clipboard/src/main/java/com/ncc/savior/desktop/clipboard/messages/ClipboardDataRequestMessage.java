package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;

/**
 * Message that requests the current active clipboard data for the given format.
 *
 */
public class ClipboardDataRequestMessage extends BaseClipboardMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private ClipboardFormat format;
	private String requestId;

	// For Jackson (de)serialization
	protected ClipboardDataRequestMessage() {
		this(null, null, null);
	}

	public ClipboardDataRequestMessage(String ownerId, ClipboardFormat format, String requestId) {
		super(ownerId, MessageType.CLIPBOARD);
		this.format = format;
		this.requestId = requestId;
	}

	public ClipboardFormat getFormat() {
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

	// For Jackson (de)serialization
	protected void setFormat(ClipboardFormat format) {
		this.format = format;
	}

	// For Jackson (de)serialization
	protected void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}
