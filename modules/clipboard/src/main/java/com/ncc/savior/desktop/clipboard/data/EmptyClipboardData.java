package com.ncc.savior.desktop.clipboard.data;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.sun.jna.Pointer;

/**
 * Represents data from an empty clipboard. This class should be used when the
 * clipboard has been emptied or when data transfer between groups is not
 * allowed.
 *
 */
public class EmptyClipboardData extends ClipboardData {

	private static final long serialVersionUID = 1L;

	public EmptyClipboardData(ClipboardFormat format) {
		super(format);
	}

	@Override
	public Pointer createWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public String toString() {
		return "EmptyClipboardData []";
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
