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
	 * Gets the memory pointer for the data on a windows machine. This memory will
	 * NOT be cleared by JNA. It is assumed the Windows clipboard will clear the
	 * data. However, if this data is pass anywhere else, it needs to be cleared
	 * manually.
	 *
	 * @return
	 */
	public abstract Pointer createWindowsData();

	/**
	 * Gets the memory pointer for the data on a linux machine. This memory will be
	 * cleared by JNA.
	 * 
	 * @return
	 */
	public abstract Pointer createLinuxData();

	// Note: Many methods were renamed return* instead of get* to avoid serializers
	// that use getters (I.E. Jackson)
	/**
	 * Gets the number of entries in Linux data. Entries can be either 8, 16, or 32
	 * bits each (1, 2, or 4 bytes). See {@link #returnLinuxEntrySizeBits()} for the
	 * size of the entry.
	 * 
	 * @return
	 */
	public abstract int returnLinuxNumEntries();

	/**
	 * must be 8, 16, or 32
	 *
	 * @return
	 */
	public abstract int returnLinuxEntrySizeBits();

	/**
	 * Returns whether the OS (the paster) using this data (not the creator/copier)
	 * can cache the data without rechecking. Typically, data copied from a Windows
	 * machine can be cached because Windows will notify on clipboard changes. Data
	 * that originated on a Linux machine cannot be cached becuase Linux does not
	 * update on clipboard changes.
	 * 
	 * @return
	 */
	public boolean isCacheable() {
		return isCacheable;
	}

	/**
	 * Length of the data in bytes from {@link #createWindowsData()}
	 * 
	 * @return
	 */
	public abstract long returnWindowsDataLengthBytes();
}
