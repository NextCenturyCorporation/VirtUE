package com.ncc.savior.desktop.xpra.protocol;

import java.io.IOException;

import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Handles sending {@link Packet}s from the Xpra client to an Xpra Server.
 *
 *
 */

public interface IPacketSender {
    public void sendPacket(Packet packet) throws IOException;
}
