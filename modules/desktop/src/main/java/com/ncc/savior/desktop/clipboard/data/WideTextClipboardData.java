package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Wide text or Unicode implementation of {@link ClipboardData}.
 *
 */
public class WideTextClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private String data;

	public WideTextClipboardData(String data) {
		super(IWindowsClipboardUser32.CF_UNICODE);
		this.data = data;
	}

	@Override
	public Pointer getWindowsData() {
		Memory winMemory = new Memory(2 * (data.getBytes().length + 1));
		winMemory.clear();
		winMemory.setWideString(0, data);
		return winMemory;
	}

	@Override
	public String toString() {
		return "WideTextClipboardData [data=" + data + "]";
	}
}
