package com.ncc.savior.desktop.xpra.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.Header;

public class RencodeEncoder implements IEncoder {
    @Override
    public List<Object> decode(InputStream input) throws IOException {
		throw new UnsupportedOperationException("Rencode is not yet implemented");
    }

    @Override
    public void encode(ByteArrayOutputStream baos, List<Object> list) throws IOException {
		throw new UnsupportedOperationException("Rencode is not yet implemented");
    }

    @Override
    public byte getProtocolFlags() {
        return Header.FLAG_RENCODE;
    }

}
