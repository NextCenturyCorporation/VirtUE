package com.ncc.savior.desktop.xpra.protocol;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.encoder.BencodeEncoder;
import com.ncc.savior.desktop.xpra.protocol.encoder.IEncoder;
import com.ncc.savior.desktop.xpra.protocol.encoder.RencodeEncoder;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketBuilder;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Read stream and convert to packets.
 * <p>
 * See https://xpra.org/trac/wiki/PacketEncoding for some details
 * <p>
 * From site: - The main chunk containing the message uses index 0 and it is
 * encoded (see bencode / rencode below). - The main message consists of a list
 * of values, the first item is the packet type (see wiki/NetworkProtocol). -
 * The other chunks replace the item found at the specified index in the main
 * chunk. (which should be empty) The main chunk always comes last.
 * <p>
 */
public class InputStreamPacketReader implements IPacketReader {
	private static Logger logger = LoggerFactory.getLogger(InputStreamPacketReader.class);

	private final InputStream in;
	private final byte[] buffer = new byte[4096];
	private final PacketBuilder packetBuilder;

	private RencodeEncoder rencoder;

	private IEncoder bencoder;

	public InputStreamPacketReader(InputStream in, PacketBuilder packetBuilder) {
		this.in = in;
		this.packetBuilder = packetBuilder;
		this.bencoder = new BencodeEncoder();
	}

	@Override
	public Packet getNextPacket() throws IOException {
		return readPacket();
	}

	private Packet readPacket() throws IOException {

		Header header = null;
		Map<Integer, byte[]> patches = new TreeMap<Integer, byte[]>();
		while (true) {
			byte[] bytes = readBytes(Header.HEADER_SIZE);

			header = new Header(bytes);
			if (header.getChunkIndex() > 0) {
				if (logger.isTraceEnabled()) {
					logger.trace("Reading chunk #" + header.getChunkIndex() + ".");
				}
				byte[] patch = readPatch(header);
				int index = header.getChunkIndex();
				patches.put(index, patch);
			} else {
				logger.trace("Reading Chunk #0.  All other chunks read");
				break;
			}
		}
		InputStream input = in;
		if (header.isCompressed()) {
			input = new ChunkInflaterInputStream(in, new Inflater(), header.getSize());
		}
		IEncoder encoder = getEncoder(header);
		List<Object> list = encoder.decode(input);

		for (Entry<Integer, byte[]> patch : patches.entrySet()) {
			list.set(patch.getKey(), patch.getValue());
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Building packet from " + list.toString());
		}
		Packet packet = packetBuilder.buildPacket(list);
		if (logger.isTraceEnabled()) {
			logger.trace("Built " + packet.toString() + " from " + list.toString());
		}
		// logger.debug("Receive:" + packet.toString());
		return packet;
	}

	private byte[] readBytes(int numBytes) throws IOException {
		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < numBytes; i++) {
			// byte[] bt = new byte[2048];
			// System.out.println(in.read(bt, 0, 8));
			int v = in.read();
			if (v < 0) {
				throw new IOException("Unable to read bytes from InputStream!");
			}
			bytes[i] = (byte) (v & 0xFF);
		}
		return bytes;
	}

	private IEncoder getEncoder(Header header) {
		// TODO more encodings?
		if (header.hasFlag(Header.FLAG_RENCODE)) {
			if (this.rencoder == null) {
				this.rencoder = new RencodeEncoder();
			}
			logger.trace("Using Rencoder");
			return this.rencoder;
		} else {
			logger.trace("Using Bencoder");
			return this.bencoder;
		}
	}

	private byte[] readPatch(Header header) throws IOException {
		if (header.isCompressed()) {
			return readCompressed(header);
		} else {
			return readUncompressed(in, header);
		}
	}

	private byte[] readUncompressed(InputStream in, Header header) throws IOException {
		byte[] buffer = new byte[header.getSize()];
		int bytesLeft = header.getSize();
		int bytesRead = 0;
		while (bytesRead < header.getSize()) {
			final int r = in.read(buffer, bytesRead, bytesLeft);
			if (r < 0) {
				throw new EOFException("Unexpected end of stream");
			}
			bytesRead += r;
			bytesLeft -= r;
		}
		return buffer;
	}

	@SuppressWarnings("resource")
	private byte[] readCompressed(Header header) throws IOException {
		ChunkInflaterInputStream iis = null;
		try {
			Inflater inflater = new Inflater();
			// TODO We can't close the steam because we need to keep using it. There is
			// probably
			// a better way to handle this or this warning wouldn't be here? maybe?
			iis = new ChunkInflaterInputStream(in, inflater, header.getSize());
			ByteArrayOutputStream output = new ByteArrayOutputStream(header.getSize());
			while (iis.available() > 0) {
				final int r = iis.read(buffer, 0, buffer.length);
				if (r < 0) {
					throw new EOFException("Unexpected end of stream");
				}
				output.write(buffer, 0, r);
			}
			iis.drain();
			return output.toByteArray();
		} finally {
			if (iis != null) {
				// iis.close();
			}
		}

	}

}
