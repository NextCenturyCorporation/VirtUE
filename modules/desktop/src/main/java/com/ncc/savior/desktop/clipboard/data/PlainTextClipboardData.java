package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Specific implementation for simple plain text clipboard data
 *
 *
 */

public class PlainTextClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private String data;

	public PlainTextClipboardData(String data) {
		super(IWindowsClipboardUser32.CF_TEXT);
		this.data = data;
	}

	@Override
	public Pointer getWindowsData() {
		Memory winMemory = new Memory(1 * (data.getBytes().length + 1));
		winMemory.clear();
		winMemory.setString(0, data);
		return winMemory;
	}

}
