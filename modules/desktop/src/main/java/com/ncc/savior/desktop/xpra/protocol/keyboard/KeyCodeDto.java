package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.ArrayList;
import java.util.List;

public class KeyCodeDto {
	private int javaFxOrdinal;
	private int keyCode;
	private int keyVal;
	private String keyName;
	private String str;

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
		return javaFxOrdinal;
	}

	public void setOrdinal(int ordinal) {
		this.javaFxOrdinal = ordinal;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		return "KeyCodeDto [javaFxOrdinal=" + javaFxOrdinal + ", keyCode=" + keyCode + ", keyName=" + keyName + ", str="
				+ str + "]";
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

	// HTML Xpra client always uses group=0;
	public int getGroup() {
		return 0;
	}

	// HTML Xpra client always uses keyVal as KeyCode
	public int getKeyVal() {
		return keyVal;
	}

	public void setKeyVal(int keyVal) {
		this.keyVal = keyVal;
	}
}