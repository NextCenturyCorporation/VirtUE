package com.ncc.savior.desktop.clipboard.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * Interface to give functions for the clipboard specific calls for Windows. See
 * windows API for documentation on thses methods.
 *
 *
 */
public interface IWindowsClipboardUser32 extends User32 {
	// DEFAULT_OPTIONS is critical for W32 API functions to simplify ASCII/UNICODE
	// details
	IWindowsClipboardUser32 INSTANCE = (IWindowsClipboardUser32) Native.loadLibrary("user32",
			IWindowsClipboardUser32.class, W32APIOptions.DEFAULT_OPTIONS);
	int CF_TEXT = 1;
	int CF_UNICODE = 13;

	boolean EmptyClipboard();

	boolean OpenClipboard(WinDef.HWND window);

	boolean CloseClipboard();

	WinDef.HWND GetClipboardOwner();

	Pointer SetClipboardData(int format, Pointer data);

	Pointer GetClipboardData(int format);

	int EnumClipboardFormats(int format);

	boolean GetUpdatedClipboardFormats(int[] formats, int sizeOfFormats, IntByReference returnedSizeOfFormats);

	WinDef.HWND SetClipboardViewer(WinDef.HWND handle);
}
