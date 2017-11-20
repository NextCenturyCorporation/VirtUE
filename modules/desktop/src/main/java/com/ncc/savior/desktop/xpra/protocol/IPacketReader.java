package com.ncc.savior.desktop.xpra.protocol;

import java.io.Closeable;
import java.io.IOException;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Handles reading, constructing and returning {@link Packet}s from an Xpra
 * server.
 *
 *
 */
public interface IPacketReader extends Closeable {
    public Packet getNextPacket() throws IOException;
}
