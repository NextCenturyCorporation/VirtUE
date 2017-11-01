package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.ArrayList;
import java.util.List;

public class KeyCodeDto {
	public int ordinal;
	public int keyCode;
	public String keyName;

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public String toString() {
		return "KeyCodeContainer{" + "ordinal=" + ordinal + ", keyCode=" + keyCode + ", keyName='" + keyName + '\''
				+ '}';
	}


	public List<Object> toList() {
		final List<Object> list = new ArrayList<>(5);
		list.add(keyCode);
		list.add(keyName);
		list.add(keyCode);
		list.add(0);
		list.add(0);
		return list;
	}
}