package com.ncc.savior.desktop.xpra.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public OutputStreamPacketSender(OutputStream outputStream, IEncoder encoder) {
		this.outputStream = outputStream;
		this.encoder = encoder;
	}

	@Override
	public synchronized void sendPacket(Packet packet) throws IOException {
		// logger.debug("Send: " + packet);
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
		packetListenerManager.handlePacket(packet);
	}

	public void setPacketListenerManager(PacketListenerManager packetSentListenerManager) {
		this.packetListenerManager = packetSentListenerManager;

	}
}
