package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

public class KeyActionPacket extends WindowPacket {

	private int keyval;
	private int keycode;
	private String keyname;
	private boolean pressed;
	private List<String> modifiers;
	private int group;
	private String str;

	public KeyActionPacket(int windowId, int keyval, int keycode, String keyname, boolean pressed, int group,
			List<String> modifiers) {
		super(windowId, PacketType.KEY_ACTION);
		this.keyval = keyval;
		this.keycode = keycode;
		this.keyname = keyname;
		this.pressed = pressed;
		this.group = group;
		this.modifiers = modifiers;
		this.str = keyname;
	}

	public KeyActionPacket(int windowId, int keyval, int keycode, String keyname, boolean pressed, int group,
			List<String> modifiers, String str) {
		super(windowId, PacketType.KEY_ACTION);
		this.keyval = keyval;
		this.keycode = keycode;
		this.keyname = keyname;
		this.pressed = pressed;
		this.group = group;
		this.modifiers = modifiers;
		if (str == null) {
			this.str = keyname;
		} else {
			this.str = str;
		}
	}

	public KeyActionPacket(List<Object> list) {
		this(PacketUtils.asInt(list.get(1)), PacketUtils.asInt(list.get(2)), PacketUtils.asInt(list.get(3)),
				PacketUtils.asString(list.get(4)), PacketUtils.asBoolean(list.get(5)), PacketUtils.asInt(list.get(6)),
				PacketUtils.asStringList(list.get(7)));
	}

	// var packet = ["key-action", this.topwindow, keyname, pressed, modifiers,
	// keyval, str, keycode, group];
	@Override
	protected void doAddToList(ArrayList<Object> list) {
		list.add(windowId);
		list.add(keyname);
		list.add(pressed);
		list.add(modifiers);
		list.add(keyval);
		list.add(str);
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
