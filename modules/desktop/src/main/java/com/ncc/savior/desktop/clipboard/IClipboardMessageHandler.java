package com.ncc.savior.desktop.clipboard;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public interface IClipboardMessageHandler {
	public void onMessage(IClipboardMessage message);
}
