package com.ncc.savior.desktop.clipboard.messages;

import java.util.Date;

public class BaseClipboardMessage implements IClipboardMessage {

	protected Date sendTime;
	protected String clipboardOwnerId;

	public BaseClipboardMessage(String ownerId) {
		this.sendTime = new Date();
		this.clipboardOwnerId = ownerId;
	}

	@Override
	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	@Override
	public String getClipboardOwnerId() {
		return clipboardOwnerId;
	}

}
