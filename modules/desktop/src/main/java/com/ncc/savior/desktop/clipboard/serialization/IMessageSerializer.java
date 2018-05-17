package com.ncc.savior.desktop.clipboard.serialization;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Serializes and deserializes messages
 *
 *
 */
public interface IMessageSerializer extends Closeable {
	public void serialize(IClipboardMessage message) throws IOException;

	public IClipboardMessage deserialize() throws IOException;
}
