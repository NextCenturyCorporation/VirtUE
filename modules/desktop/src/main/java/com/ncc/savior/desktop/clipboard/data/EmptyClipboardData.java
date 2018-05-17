package com.ncc.savior.desktop.clipboard.data;

import com.sun.jna.Pointer;

public class EmptyClipboardData extends ClipboardData {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public EmptyClipboardData(int format) {
		super(format);
	}

	@Override
	public Pointer getWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public String toString() {
		return "EmptyClipboardData []";
	}

}
