package com.ncc.savior.desktop.clipboard.data;

import com.sun.jna.Pointer;

public abstract class ClipboardData {
	private int format;

	protected ClipboardData(int format) {
		this.format=format;
	}

	public int getFormat() {
		return format;
	}

	public abstract Pointer getWindowsData();
}
