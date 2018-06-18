package com.ncc.savior.desktop.clipboard.data;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.sun.jna.Pointer;

/**
 * Generic unknown data implementation.
 *
 * Should probably not be used other than for testing and debugging.
 *
 */
public class UnknownClipboardData extends ClipboardData {

	private static final long serialVersionUID = 1L;

	public UnknownClipboardData(ClipboardFormat cf) {
		super(cf);
	}

	@Override
	public Pointer createWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public Pointer createLinuxData() {
		return Pointer.NULL;
	}

	@Override
	public int getLinuxNumEntries() {
		return 0;
	}

	@Override
	public int getLinuxEntrySizeBits() {
		return 8;
	}

	@Override
	public long getWindowsDataLengthBytes() {
		return 0;
	}
}