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

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
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
				+ messageSourceId + "]";
	}

}
