package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

/**
 * Packet to inform the server that a mouse button state has changed. Modifiers
 * can be a set of the following:
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
public class MouseButtonActionPacket extends Packet {
	private List<Integer> position;
	private List<String> modifiers;
	private int windowId;
	private boolean pressed;
	private int button;

	public MouseButtonActionPacket(int windowId, int button, boolean pressed, int x, int y, List<String> modifiers) {
		super(PacketType.BUTTON_ACTION);
		this.windowId = windowId;
		this.button = button;
		this.pressed = pressed;
		this.position = new ArrayList<Integer>(2);
		this.position.add(x);
		this.position.add(y);
		this.modifiers = modifiers;
	}

	public MouseButtonActionPacket(int windowId, int button, boolean pressed, int x, int y) {
		this(windowId, button, pressed, x, y, new ArrayList<String>(2));
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(button);
		list.add(pressed);
		list.add(position);
		list.add(modifiers);
	}

	@Override
	public String toString() {
		return "MouseButtonAction [position=" + position + ", modifiers=" + modifiers + ", windowId=" + windowId
				+ ", pressed=" + pressed + ", button=" + button + ", type=" + type + "]";
	}
}
