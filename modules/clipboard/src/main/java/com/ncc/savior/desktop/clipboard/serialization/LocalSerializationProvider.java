package com.ncc.savior.desktop.clipboard.serialization;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.messages.IClipboardMessage;

public class LocalSerializationProvider {
	private static final Logger logger = LoggerFactory.getLogger(LocalSerializationProvider.class);

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
			logger.debug("Local offer (" + outBound.size() + "): " + message);
			boolean success = outBound.offer(message);
			logger.debug("Local offered (" + outBound.size() + "): ");

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
				logger.debug("Local take (" + outBound.size() + "):");
				message = inBound.take();
				logger.debug("Local taken (" + outBound.size() + "): message");
			} catch (InterruptedException e) {
				logger.warn("Thread interrupted unexpectedly.  Continueing anyway.", e);
				message = deserialize();
			}
			return message;
		}
	}
}
