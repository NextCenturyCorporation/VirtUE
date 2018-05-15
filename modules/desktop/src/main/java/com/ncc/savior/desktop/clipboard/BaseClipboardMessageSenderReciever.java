package com.ncc.savior.desktop.clipboard;

public abstract class BaseClipboardMessageSenderReciever implements IClipboardMessageSenderReciever {

	protected IClipboardMessageHandler clipboardMessageHandler;

	@Override
	public void setMessageFromHubHandler(IClipboardMessageHandler clipboardMessageHandler) {
		this.clipboardMessageHandler = clipboardMessageHandler;
	}

}
