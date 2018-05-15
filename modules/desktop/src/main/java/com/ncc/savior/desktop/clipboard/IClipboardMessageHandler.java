package com.ncc.savior.desktop.clipboard;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Interface to handle {@link IClipboardMessage}s
 *
 *
 */
public interface IClipboardMessageHandler {
	/**
	 * Called when an {@link IClipboardMessage} is received
	 * 
	 * @param message
	 */
	public void onMessage(IClipboardMessage message);
}
