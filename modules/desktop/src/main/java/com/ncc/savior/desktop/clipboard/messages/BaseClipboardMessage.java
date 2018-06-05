package com.ncc.savior.desktop.clipboard.messages;

import java.io.Serializable;
import java.util.Date;

/**
 * Base class for a message between clipboard clients and the hub. This is the
 * basis of communication between them.
 *
 *
 */
public class BaseClipboardMessage implements IClipboardMessage, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	protected Date sendTime;
	protected String messageSourceId;

	public BaseClipboardMessage(String messageSourceId) {
		this.sendTime = new Date();
		this.messageSourceId = messageSourceId;
	}

	@Override
	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	@Override
	public String getSourceId() {
		return messageSourceId;
	}

}
