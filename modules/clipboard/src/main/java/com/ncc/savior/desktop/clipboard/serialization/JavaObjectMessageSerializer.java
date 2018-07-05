package com.ncc.savior.desktop.clipboard.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.connection.IConnectionWrapper;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;
import com.ncc.savior.util.JavaUtil;

/**
 * This implementation of {@link IMessageSerializer} uses Java Object
 * Serialization and the {@link Serializable} interface to serialize the
 * {@link IClipboardMessage}s. It passes or receives them from streams provided
 * by an {@link IConnectionWrapper} passed into the constructor.
 *
 *
 */
public class JavaObjectMessageSerializer implements IMessageSerializer {
	private static final Logger logger = LoggerFactory.getLogger(JavaObjectMessageSerializer.class);

	private ObjectInputStream in;
	private ObjectOutputStream out;

	private IConnectionWrapper connection;

	private Object readLock;

	private Object writeLock;

	public JavaObjectMessageSerializer(IConnectionWrapper connection) throws IOException {
		this.readLock = new Object();
		this.writeLock = new Object();
		this.connection = connection;
		out = new ObjectOutputStream(connection.getOutputStream());
		out.flush();
		in = new ObjectInputStream(connection.getInputStream());
	}

	@Override
	public void serialize(IClipboardMessage message) throws IOException {
		synchronized (writeLock) {
			out.writeObject(message);
			out.flush();
		}
	}

	@Override
	public IClipboardMessage deserialize() throws IOException {
		try {
			Object obj = null;
			synchronized (readLock) {
				obj = in.readObject();
			}
			if (obj instanceof IClipboardMessage) {
				return (IClipboardMessage) obj;
			} else {
				logger.error("Error deserializing message.  Object did not implement IClipboardMessage.  Obj=" + obj);
				return null;
			}
		} catch (ClassNotFoundException e) {
			logger.error("Error deserializing message.  Class not found!", e);
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		logger.debug("Closing " + this);
		JavaUtil.closeIgnoreErrors(connection);
	}

}
