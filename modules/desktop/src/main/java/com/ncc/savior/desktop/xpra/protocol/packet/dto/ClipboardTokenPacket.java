package com.ncc.savior.desktop.xpra.protocol.packet.dto;

import java.util.List;

import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketUtils;

/**
 * Packet that indicates that the server's clipboard has changed and contains
 * the clipboard. Only works for string messages.
 *
 *
 */
public class ClipboardTokenPacket extends Packet {
	private String clipboard;
	private List<String> attributes;
	// not sure what this does yet.
	private String string1;
	private String string2;
	private long size;
	private String sizeUnit;
	private String value;
	private long unknown;
	private long unknown2;

	protected ClipboardTokenPacket() {
		super(PacketType.CLIPBOARD_TOKEN);
	}

	public ClipboardTokenPacket(List<Object> list) {
		super(PacketType.CLIPBOARD_TOKEN);
		this.clipboard = PacketUtils.asString(list.get(1));
		this.attributes = PacketUtils.asStringList(list.get(2));
		if (list.size() > 3) {
			this.string1 = PacketUtils.asString(list.get(3));
			this.string2 = PacketUtils.asString(list.get(4));
			this.size = PacketUtils.asLong(list.get(5));
			this.sizeUnit = PacketUtils.asString(list.get(6));
			this.value = PacketUtils.asString(list.get(7));
			this.unknown = PacketUtils.asLong(list.get(8));
			this.unknown2 = PacketUtils.asLong(list.get(9));
		}
	}

	@Override
	protected void doAddToList(List<Object> list) {
		list.add(clipboard);
		list.add(attributes);
		if (value != null) {
			list.add(string1);
			list.add(string2);
			list.add(size);
			list.add(sizeUnit);
			list.add(value);
			list.add(unknown);
			list.add(unknown2);
		}
	}

	public String getClipboard() {
		return clipboard;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public String getString1() {
		return string1;
	}

	public String getString2() {
		return string2;
	}

	public long getSize() {
		return size;
	}

	public String getSizeUnit() {
		return sizeUnit;
	}

	public String getValue() {
		return value;
	}

	public long getUnknown() {
		return unknown;
	}

	public long getUnknown2() {
		return unknown2;
	}

	@Override
	public String toString() {
		return "ClipboardTokenPacket [clipboard=" + clipboard + ", attributes=" + attributes + ", string1=" + string1
				+ ", string2=" + string2 + ", size=" + size + ", sizeUnit=" + sizeUnit + ", value=" + value
				+ ", unknown=" + unknown + ", unknown2=" + unknown2 + ", type=" + type + "]";
	}

}
