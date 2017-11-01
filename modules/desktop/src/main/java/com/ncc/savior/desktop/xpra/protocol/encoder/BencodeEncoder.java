package com.ncc.savior.desktop.xpra.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.Header;

public class BencodeEncoder implements IEncoder {
	private Bencoder bencoder;

	public BencodeEncoder() {
		bencoder = new Bencoder();
	}

	@SuppressWarnings("unchecked")
	@Override
    public List<Object> decode(InputStream input) throws IOException {
		Object obj = bencoder.decodeStream(input);
		if (obj instanceof List) {
			return (List<Object>) obj;
		} else {
			throw new IOException("Stream decoded to wrong object type.  Type="+obj.getClass().getCanonicalName());
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
