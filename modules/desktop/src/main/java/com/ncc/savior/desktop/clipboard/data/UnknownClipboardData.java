package com.ncc.savior.desktop.clipboard.data;

import com.sun.jna.Pointer;

public class UnknownClipboardData extends ClipboardData {

	public UnknownClipboardData(int format) {
		super(format);
	}

	@Override
	public Pointer getWindowsData() {
		return Pointer.NULL;
	}

}
