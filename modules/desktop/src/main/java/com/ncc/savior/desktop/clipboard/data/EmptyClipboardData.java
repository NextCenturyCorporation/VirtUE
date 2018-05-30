package com.ncc.savior.desktop.clipboard.data;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
	public Pointer getWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public String toString() {
		return "EmptyClipboardData []";
	}

	@Override
	public int getLinuxData(PointerByReference pbr) {
		pbr.setPointer(Pointer.NULL);
		return 0;
	}
}
