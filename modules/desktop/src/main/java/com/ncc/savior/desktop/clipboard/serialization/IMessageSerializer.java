package com.ncc.savior.desktop.clipboard.serialization;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Abstraction for class that serializes and deserializes
 * {@link IClipboardMessage}s and passes them on. Typically
 * {@link IMessageSerializer}s have an {@link IConnectionWrapper} to pass the
 * serialized messages on.
 *
 *
 */
public interface IMessageSerializer extends Closeable {
	/**
	 * Serializes the message and passes it on to an implementation specific stream.
	 * The stream should be flushed before returning. Therefore, this method could
	 * block if the underlying serialization or stream does not complete
	 * immediately.
	 *
	 * @param message
	 * @throws IOException
	 */
	public void serialize(IClipboardMessage message) throws IOException;

	/**
	 * Blocks and deserializes next message from implementation specific stream.
	 *
	 * @return
	 * @throws IOException
	 */
	public IClipboardMessage deserialize() throws IOException;
}
