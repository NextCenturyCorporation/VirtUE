package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Packet to tell the server where the mouse is.
 *
 * Modifiers can be a set of the following:
 * <ul>
 * <li>shift
 * <li>control
 * <li>alt
 * </ul>
 *
 * There are probably other modifiers
 *
 *
 */
public class MousePointerPositionPacket extends WindowPacket {

	private List<Integer> position;
	private List<String> modifiers;

	public MousePointerPositionPacket(int windowId, int x, int y, List<String> modifiers) {
		super(windowId, PacketType.POINTER_POSITION);
		position = new ArrayList<Integer>(2);
		position.add(x);
		position.add(y);
		this.modifiers = modifiers;
	}

	public MousePointerPositionPacket(int windowId, int x, int y) {
		this(windowId, x, y, new ArrayList<String>(0));
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(position);
		list.add(modifiers);
		// list.add(new ArrayList<String>());
	}

	@Override
	public String toString() {
		return "MousePointerPositionPacket [position=" + position + ", modifiers=" + modifiers + ", windowId="
				+ windowId + ", type=" + type + "]";
	}

}
