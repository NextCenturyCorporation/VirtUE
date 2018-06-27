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
	private MessageType messageType;

	// For Jackson (de)serialization
	protected BaseClipboardMessage() {
		this(null, null);
	}

	public BaseClipboardMessage(String messageSourceId, MessageType type) {
		this.messageType = type;
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

	public void setSourceId(String messageSourceId) {
		this.messageSourceId = messageSourceId;
	}

	@Override
	public MessageType getType() {
		return messageType;
	}

}
