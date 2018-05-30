package com.ncc.savior.desktop.clipboard.data;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
	public Pointer getWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public int getLinuxData(PointerByReference pbr) {
		pbr.setPointer(Pointer.NULL);
		return 0;
	}

}
