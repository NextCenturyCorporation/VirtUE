package com.ncc.savior.desktop.clipboard.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	@JsonIgnore
	@Override
	public Pointer createWindowsData() {
		return Pointer.NULL;
	}

	@Override
	public String toString() {
		return "EmptyClipboardData []";
	}

	@JsonIgnore
	@Override
	public Pointer createLinuxData() {
		return Pointer.NULL;
	}

	@JsonIgnore
	@Override
	public int returnLinuxNumEntries() {
		return 0;
	}

	@JsonIgnore
	@Override
	public int returnLinuxEntrySizeBits() {
		return 8;
	}
}
