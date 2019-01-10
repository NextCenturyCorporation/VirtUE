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
