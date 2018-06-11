package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.sun.jna.Pointer;

/**
 * Base class for clipboard data.
 *
 */
public abstract class ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private ClipboardFormat format;
	private boolean isCacheable;

	protected ClipboardData(ClipboardFormat format) {
		this.format = format;
		this.isCacheable = false;
	}

	/**
	 * Gets the format of the clipboard. Currently this is windows values, but once
	 * we incorporate linux, we'll need some generic class or something that can
	 * convert between OSs.
	 *
	 * @return
	 */
	public ClipboardFormat getFormat() {
		return format;
	}

	/**
	 * Gets the memory pointer for the data on a windows machine.
	 *
	 * @return
	 */
	public abstract Pointer createWindowsData();

	public abstract Pointer createLinuxData();

	public abstract int returnLinuxNumEntries();

	/**
	 * must be 8, 16, or 32
	 *
	 * @return
	 */
	public abstract int returnLinuxEntrySizeBits();

	public boolean isCacheable() {
		return isCacheable;
	}

	public abstract long returnWindowsDataLengthBytes();
}
