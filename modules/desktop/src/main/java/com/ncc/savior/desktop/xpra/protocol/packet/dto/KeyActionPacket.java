package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;

public class KeyActionPacket extends WindowPacket {

	private int keyval;
	private int keycode;
	private String keyname;
	private boolean pressed;
	private List<String> modifiers;
	private int group;

	public KeyActionPacket(int windowId, int keyval, int keycode, String keyname, boolean pressed, int group,
			List<String> modifiers) {
		super(windowId, PacketType.KEY_ACTION);
		this.keyval = keyval;
		this.keycode = keycode;
		this.keyname = keyname;
		this.pressed = pressed;
		this.group = group;
		this.modifiers = modifiers;
	}

	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(keyname);
		list.add(pressed);
		list.add(modifiers);
		list.add(keyval);
		list.add(keyname);
		list.add(keycode);
		list.add(group);
	}

	@Override
	public String toString() {
		return "KeyActionPacket [keyval=" + keyval + ", keycode=" + keycode + ", keyname=" + keyname + ", pressed="
				+ pressed + ", modifiers=" + modifiers + ", group=" + group + ", windowId=" + windowId + ", type="
				+ type + "]";
	}
}
