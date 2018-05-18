package com.ncc.savior.desktop.clipboard;

import java.io.IOException;

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
	 * @param groupId
	 */
	public void onMessage(IClipboardMessage message, String groupId);

	public void onMessageError(IOException e);

}
