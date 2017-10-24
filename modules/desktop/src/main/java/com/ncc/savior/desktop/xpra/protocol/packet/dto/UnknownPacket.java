package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * This class is used when the Xpra server sends a Packet that is not known or
 * not yet implemented by the client.
 *
 *
 */
public class UnknownPacket extends Packet {
    private final List<Object> list;

    public UnknownPacket(List<Object> list) {
		super(PacketType.UNKNOWN);
        this.list = list;
    }

    @Override
    public String toString() {
        return "UnknownPacket{" +
                "list=" + list +
                '}';
    }

    @Override
    protected void doAddToList(ArrayList<Object> list) {
        //do nothing
    }
}
