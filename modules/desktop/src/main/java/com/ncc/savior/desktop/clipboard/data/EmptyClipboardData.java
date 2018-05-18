package com.ncc.savior.desktop.clipboard.data;

import com.sun.jna.Pointer;

/**
 * Represents data from an empty clipboard. This class should be used when the
 * clipboard has been emptied or when data transfer between groups is not
 * allowed.
 *
 */
public class EmptyClipboardData extends ClipboardData {

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
