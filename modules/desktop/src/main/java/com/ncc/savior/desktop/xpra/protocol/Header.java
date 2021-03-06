/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.protocol;

import java.io.IOException;

/**
 * The Xpra protocol sends Packets in one or more parts which contains a header
 * chunk and a data chunk. This class represents the header chunk.
 *
 * <p>
 * See https://xpra.org/trac/wiki/PacketEncoding for details of packet encodings
 * <p>
 * Headers are 8 bytes containing the following:
 * <ol>
 * <li>magic value "P" (80 decimal, 0x50 in hex)
 * <li>one byte for protocol flags
 * <li>on byte for compression level hint
 * <li>on byte for chunk index (0 for main)
 * <li>on long (4 bytes) for size of the data that follows
 * </ol>
 */
public class Header {

	public static final int HEADER_SIZE = 8;
	private static final byte MAGIC_BYTE = 'P';

	// assume BENCODE if no flag is set
	public static final int FLAG_BENCODE = 0x0;
	public static final int FLAG_RENCODE = 0x1;
	public static final int FLAG_CIPHER = 0x2;
	public static final int FLAG_YAML = 0x4;

	// low bits contain compression level, high bits compression algorithm
	public static final int FLAG_ZLIB = 0x0;
	public static final int FLAG_LZ4 = 0x10;
	public static final int FLAG_LZO = 0x20;
	public static final int FLAGS_NOHEADER = 0x40;

	private final byte[] byteArray;
	// private final byte magicByte;
	private final byte protocolFlagsByte;
	private final byte compressionByte;
	private final byte chunkIndex;
	private final int size;

	public Header(byte[] bytes) throws IOException {
		if (bytes[0] != MAGIC_BYTE) {
			throw new IOException("Incorrect Magic Byte.  FirstByte=" + byteToString(bytes[0])
					+ ", Expected=" + byteToString(MAGIC_BYTE));
		}
		// magicByte = bytes[0];
		protocolFlagsByte = bytes[1];
		compressionByte = bytes[2];
		chunkIndex = bytes[3];
		size = (bytes[4] & 0xFF) << 24 | (bytes[5] & 0xFF) << 16 | (bytes[6] & 0xFF) << 8 | (bytes[7] & 0xFF);
		this.byteArray = bytes;
	}

	private String byteToString(byte b) {
		return Byte.toString(b) + "(" + String.format("%02X ", b) + ")";
	}

	public static Header createHeader(byte protocolFlags, byte compressionFlags, byte chunkIndex, long size)
			throws IOException {
		byte[] bytes = new byte[HEADER_SIZE];
		bytes[0] = MAGIC_BYTE;
		bytes[1] = protocolFlags;
		bytes[2] = compressionFlags;
		bytes[3] = chunkIndex;
		bytes[4] = (byte) ((size >>> 24) & 0xFF);
		bytes[5] = (byte) ((size >>> 16) & 0xFF);
		bytes[6] = (byte) ((size >>> 8) & 0xFF);
		bytes[7] = (byte) (size & 0xFF);
		return new Header(bytes);
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public byte getProtocolFlagsByte() {
		return protocolFlagsByte;
	}

	public byte getCompressionLevelByte() {
		return compressionByte;
	}

	public byte getChunkIndex() {
		return chunkIndex;
	}

	public int getSize() {
		return size;
	}

	public boolean isCompressed() {
		return this.compressionByte > 0;
	}

	public boolean hasFlag(int flag) {
		return (protocolFlagsByte & flag) > 0;
	}
}
