package com.ncc.savior.desktop.xpra.protocol.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Xpra supports multiple encodings of the Packet data. The encodings are:
 * <ul>
 * <li>Bencode
 * <li>Rencode
 * </ul>
 *
 * Rencode is supposedly more performance than Bencode.
 * <p>
 * More encodings may be implemented in the future.
 *
 */
public interface IEncoder {
    List<Object> decode(InputStream input) throws IOException;

    void encode(ByteArrayOutputStream baos, List<Object> list) throws IOException;

    byte getProtocolFlags();
}
