package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.NativelyDeallocatedMemory;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Wide text or Unicode implementation of {@link ClipboardData}.
 *
 */
public class UnicodeClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private String data;

	public UnicodeClipboardData(String data) {
		super(ClipboardFormat.UNICODE);
		this.data = data;
	}

	@Override
	public Pointer createWindowsData(WindowsClipboardWrapper wrapper) {
		Memory winMemory = new NativelyDeallocatedMemory(getWindowsDataLengthBytes());
		winMemory.clear();
		winMemory.setWideString(0, data);
		return winMemory;
	}

	@Override
	public String toString() {
		return "UnicodeClipboardData [data=" + data + "]";
	}

	@Override
	public Pointer createLinuxData() {
		int size = Native.WCHAR_SIZE * (data.length() + 1);
		Memory mem = new Memory(size);
		mem.clear();
		mem.setString(0, data, "UTF-8");
		return mem;
	}

	@Override
	public int getLinuxNumEntries() {
		return data.length();
	}

	@Override
	public int getLinuxEntrySizeBits() {
		return 8;
	}

	@Override
	public long getWindowsDataLengthBytes() {
		return Native.WCHAR_SIZE * (data.length() + 1);
	}
}
