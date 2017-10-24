package com.ncc.savior.desktop.xpra.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Modified Inflater Stream to handle Xpra compression
 */
class ChunkInflaterInputStream extends InflaterInputStream {

    private int bytesToDrain;

    ChunkInflaterInputStream(InputStream in, Inflater inf, int size) {
        super(in, inf, size);
        this.bytesToDrain = size;
    }

    @Override
    protected void fill() throws IOException {
        final int toRead = Math.min(buf.length, bytesToDrain);
        len = in.read(buf, 0, toRead);
        if (len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        inf.setInput(buf, 0, len);
        bytesToDrain -= len;
    }

    void drain() throws IOException {
        if (bytesToDrain < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        int toRead = bytesToDrain;
        while (toRead > 0) {
            int len = toRead;
            if (len > buf.length) {
                len = buf.length;
            }
            len = in.read(buf, 0, len);
            if (len < 0) {
                throw new EOFException("Unexpected end of input stream");
            }
            toRead -= len;
        }
    }

    @Override
    public int available() throws IOException {
        return inf.finished() ? 0 : 1;
    }
}
