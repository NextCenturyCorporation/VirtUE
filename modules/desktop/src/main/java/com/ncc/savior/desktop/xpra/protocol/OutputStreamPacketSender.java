package com.ncc.savior.desktop.xpra.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.IConnectionErrorCallback;
import com.ncc.savior.desktop.xpra.protocol.encoder.IEncoder;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketListenerManager;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * This class handles sending Xpra packets over an {@link OutputStream}. Sending
 * packets is synchronized so an instance is thread-safe. Any
 * {@link IPacketHandler}s attached will be called after the packet is sent.
 *
 *
 */
public class OutputStreamPacketSender implements IPacketSender {
	private static final Logger logger = LoggerFactory.getLogger(OutputStreamPacketSender.class);

	private final OutputStream outputStream;
	private final IEncoder encoder;

	private PacketListenerManager packetListenerManager;

	private boolean threaded;

	private ArrayBlockingQueue<Packet> queue;

	private Thread sendingThread;

	protected boolean stopSendingThread = false;

	private static int threadCount = 1;

	public OutputStreamPacketSender(OutputStream outputStream, IEncoder encoder,
			IConnectionErrorCallback errorCallback) {
		this.outputStream = outputStream;
		this.encoder = encoder;
		this.threaded = true;
		if (threaded) {
			this.queue = new ArrayBlockingQueue<Packet>(50);
			Runnable packetSendingRunnable = new Runnable() {
				@Override
				public void run() {
					while (!stopSendingThread) {
						// use poll if we add a method to stop the thread
						// queue.poll(1, TimeUnit.SECONDS);
						Packet packet;
						try {
							packet = queue.take();
							doSendPacket(packet);
						} catch (InterruptedException e) {
							// logger.debug("Packet Sender was interrupted!", e);
							errorCallback.onError("PacketSender", new IOException(e));
						} catch (IOException e) {
							errorCallback.onError("PacketSender", e);
						}

					}
				}
			};
			this.sendingThread = new Thread(packetSendingRunnable, "PacketSender-" + threadCount++);
			sendingThread.setDaemon(true);
			sendingThread.start();
		}
	}

	@Override

	public void sendPacket(Packet packet) throws IOException {
		if (threaded) {
			try {
				boolean success = queue.offer(packet, 100, TimeUnit.MILLISECONDS);
				if (!success) {
					// This should never happen as the queue should be read off quickly and if there
					// is a connection error, the windows should be closed.
					logger.error("Packet sending timed out! This should never happen!" + packet);
				}
			} catch (InterruptedException e) {
				logger.debug("Packet Sender interrupted.  This may be intentionally by a close().", e);
			}
		} else {
			doSendPacket(packet);
		}

	}

	public synchronized void doSendPacket(Packet packet) throws IOException {
		// if (!packet.getType().equals(PacketType.PING_ECHO)) {
		// if (!packet.getType().equals(PacketType.DAMAGE_SEQUENCE)) {
		// if (!packet.getType().equals(PacketType.POINTER_POSITION)) {
		// logger.debug("Send: " + packet);
		// }
		// }
		// }
		List<Object> list = packet.toList();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoder.encode(baos, list);
		byte[] ba = baos.toByteArray();

		Header header = Header.createHeader(encoder.getProtocolFlags(), (byte) 0, (byte) 0, ba.length);
		byte[] hba = header.getByteArray();
		if (logger.isTraceEnabled()) {
			logger.trace("HEADER: " + Arrays.toString(hba));
			logger.trace("BODY: " + Arrays.toString(ba));
		}
		outputStream.write(hba);
		outputStream.write(ba);
		outputStream.flush();
		if (logger.isTraceEnabled()) {
			logger.trace("Packet Sent: " + packet.toString());
		}
		// logger.debug("Sent: " + packet);
		packetListenerManager.handlePacket(packet);
	}

	public void setPacketListenerManager(PacketListenerManager packetSentListenerManager) {
		this.packetListenerManager = packetSentListenerManager;

	}

	@Override
	public void close() throws IOException {
		outputStream.close();
		if (threaded && sendingThread != null) {
			stopSendingThread = true;
			sendingThread.interrupt();
		}

	}
}
