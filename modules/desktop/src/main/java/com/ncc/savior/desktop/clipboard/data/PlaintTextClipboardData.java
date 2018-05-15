package com.ncc.savior.desktop.clipboard.data;

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class PlaintTextClipboardData extends ClipboardData {

	private String data;
	private Memory winMemory;

	public PlaintTextClipboardData(String data) {
		super(IWindowsClipboardUser32.CF_TEXT);
		this.data = data;
	}

	@Override
	public Pointer getWindowsData() {
		this.winMemory= new Memory(1 * (data.getBytes().length + 1));
		winMemory.clear();
		winMemory.setString(0, data);
		return winMemory;
	}

}
