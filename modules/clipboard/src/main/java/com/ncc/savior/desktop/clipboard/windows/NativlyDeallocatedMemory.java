package com.ncc.savior.desktop.clipboard.windows;

import com.sun.jna.Memory;

/**
 * {@link Memory} where the finalize is overriden such that the native memory
 * will NOT be cleaned up. Should only be used for memory that is passed to a
 * native function will clean it up like the clipboard.
 * 
 *
 */
public class NativlyDeallocatedMemory extends Memory {
	public NativlyDeallocatedMemory(long size) {
		super(size);
	}

	@Override
	protected void finalize() {
		// do nothing, aka explicitly do not call dispose.
	}
}
