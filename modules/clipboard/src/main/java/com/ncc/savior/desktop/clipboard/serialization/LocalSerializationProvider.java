package com.ncc.savior.desktop.clipboard.serialization;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

/**
 * Utility class that handles providing {@link IMessageSerializer} for a local
 * {@link ClipboardClient}. These {@link IMessageSerializer} don't actually
 * serialize the data, but instead just pass the messages over
 * {@link BlockingQueue}s. This is easier and more efficient for passing
 * {@link IClipboardMessage}s inside a JVM.
 * 
 *
 */
public class LocalSerializationProvider {
	private static final Logger logger = LoggerFactory.getLogger(LocalSerializationProvider.class);

	/**
	 * Returns a pair of serializers that can be used for the two endpoints (client
	 * and hub) of a clipboard connection when both are inside the same JVM.
	 * 
	 * @return
	 */
	@SuppressWarnings("resource")
	public static SerializerContainer createSerializerPair() {
		BlockingQueue<IClipboardMessage> queueAToB = new ArrayBlockingQueue<IClipboardMessage>(10);
		BlockingQueue<IClipboardMessage> queueBToA = new ArrayBlockingQueue<IClipboardMessage>(10);
		QueueSerializer serializerA = new QueueSerializer(queueBToA, queueAToB);
		QueueSerializer serializerB = new QueueSerializer(queueAToB, queueBToA);
		SerializerContainer container = new SerializerContainer();
		container.serializerA = serializerA;
		container.serializerB = serializerB;
		return container;
	}

	public static class SerializerContainer {
		public IMessageSerializer serializerA;
		public IMessageSerializer serializerB;
	}

	/**
	 * Serializer that doesn't actually serialize, but reads and writes to
	 * {@link BlockingQueue}s.
	 * 
	 *
	 */
	public static class QueueSerializer implements IMessageSerializer {

		private BlockingQueue<IClipboardMessage> inBound;
		private BlockingQueue<IClipboardMessage> outBound;

		public QueueSerializer(BlockingQueue<IClipboardMessage> inBound, BlockingQueue<IClipboardMessage> outBound) {
			this.inBound = inBound;
			this.outBound = outBound;
		}

		@Override
		public void close() throws IOException {
			// do nothing
		}

		@Override
		public void serialize(IClipboardMessage message) throws IOException {
			// logger.debug("Local offer (" + outBound.size() + "): " + message);
			boolean success = outBound.offer(message);
			// logger.debug("Local offered (" + outBound.size() + "): ");

			// these should always be really fast so this should never happen. We should
			// probably use a blocking call, but for now I want to know if we are ever
			// waiting on messages.
			if (!success) {
				throw new RuntimeException("internal clipboard message failed!");
			}
		}

		@Override
		public IClipboardMessage deserialize() throws IOException {
			IClipboardMessage message;
			try {
				// logger.debug("Local take (" + outBound.size() + "):");
				message = inBound.take();
				// logger.debug("Local taken (" + outBound.size() + "): message");
			} catch (InterruptedException e) {
				logger.warn("Thread interrupted unexpectedly.  Continueing anyway.", e);
				message = deserialize();
			}
			return message;
		}
	}
}
