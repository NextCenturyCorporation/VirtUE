package com.ncc.savior.desktop.clipboard.messages;

/**
 * Tells the recipient that it should extend its normal timeout for the current
 * operation. This typically occurs when we want to ask the the user if they
 * want to pass the clipboard.
 * 
 *
 */
public class ExtendTimeoutClipboardMessage extends BaseClipboardMessage {
	private static final long serialVersionUID = 1L;
	private String requestId;

	public ExtendTimeoutClipboardMessage(String sourceId, String requestId) {
		super(sourceId);
		this.requestId = requestId;
	}

	protected ExtendTimeoutClipboardMessage() {
		super(null);
	}

	public String getRequestId() {
		return requestId;
	}
}
