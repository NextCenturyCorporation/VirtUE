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

	/**
	 * Called when there is an error trying to send or receive a clipboard message.
	 *
	 * @param e
	 */
	public void onMessageError(IOException e);

}
