package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.sun.jna.Pointer;

/**
 * Base class for clipboard data.
 *
 */
public abstract class ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private int format;

	protected ClipboardData(int format) {
		this.format = format;
	}

	/**
	 * Gets the format of the clipboard. Currently this is windows values, but once
	 * we incorporate linux, we'll need some generic class or something that can
	 * convert between OSs.
	 *
	 * @return
	 */
	public int getFormat() {
		return format;
	}

	/**
	 * Gets the memory pointer for the data on a windows machine.
	 *
	 * @return
	 */
	public abstract Pointer getWindowsData();
}
