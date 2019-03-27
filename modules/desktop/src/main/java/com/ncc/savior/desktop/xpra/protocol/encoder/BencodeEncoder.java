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
package com.ncc.savior.desktop.xpra.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.Header;

public class BencodeEncoder implements IEncoder {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(BencodeEncoder.class);
	private Bencoder bencoder;

	public BencodeEncoder() {
		bencoder = new Bencoder();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> decode(InputStream input) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream bis = new InputStream() {

			@Override
			public int read() throws IOException {
				int val = input.read();
				byte[] b = new byte[] { (byte) val };
				sb.append(new String(b));
				return val;
			}
		};
		Object obj = bencoder.decodeStream(bis);
		if (obj instanceof List) {
			List<Object> list = (List<Object>) obj;
			// logger.debug(list.toString());
			// logger.debug(sb.toString());
			return list;
		} else {
			throw new IOException("Stream decoded to wrong object type.  Type=" + obj.getClass().getCanonicalName());
		}
	}

	@Override
	public void encode(ByteArrayOutputStream os, List<Object> list) throws IOException {
		try {
			bencoder.encodeList(list, os);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte getProtocolFlags() {
		return Header.FLAG_BENCODE;
	}
}
