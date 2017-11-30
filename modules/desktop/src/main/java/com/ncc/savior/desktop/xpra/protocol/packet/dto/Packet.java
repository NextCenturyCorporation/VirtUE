package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Base Packet class.
 *
 *
 */
public abstract class Packet {
	protected PacketType type;

    protected Packet(PacketType type) {
        this.type = type;
    }

    public List<Object> toList() {
        ArrayList<Object> list = new ArrayList<Object>();
        list.add(type.getLabel());
        doAddToList(list);
        return list;
    }

	protected abstract void doAddToList(List<Object> list);

	public PacketType getType() {
		return type;
	}
}
