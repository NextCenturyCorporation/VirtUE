package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.data.ClipboardData;

/**
 * Message containing {@link ClipboardData}. This is often sent in response to a
 * {@link ClipboardDataRequestMessage}.
 *
 *
 */
public class ClipboardDataMessage extends BaseClipboardMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	private ClipboardData data;
	private String requestId;
	private String destinationId;

	public ClipboardDataMessage(String ownerId, ClipboardData data, String requestId, String destinationId) {
		super(ownerId);
		this.data = data;
		this.requestId = requestId;
		this.destinationId = destinationId;
	}

	public ClipboardData getData() {
		return data;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	@Override
	public String toString() {
		return "ClipboardDataMessage [data=" + data + ", requestId=" + requestId + ", destinationId=" + destinationId
				+ ", sendTime=" + sendTime + ", messageSourceId=" + messageSourceId + "]";
	}

}
