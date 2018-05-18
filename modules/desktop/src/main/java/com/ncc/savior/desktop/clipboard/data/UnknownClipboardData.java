package com.ncc.savior.desktop.clipboard.data;

import com.sun.jna.Pointer;

/**
 * Generic unknown data implementation.
 *
 * Should probably not be used other than for testing and debugging.
 *
 */
public class UnknownClipboardData extends ClipboardData {

	private static final long serialVersionUID = 1L;

	public UnknownClipboardData(int format) {
		super(format);
	}

	@Override
	public Pointer getWindowsData() {
		return Pointer.NULL;
	}

}
