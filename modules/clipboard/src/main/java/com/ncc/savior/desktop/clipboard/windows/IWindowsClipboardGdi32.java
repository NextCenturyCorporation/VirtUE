package com.ncc.savior.desktop.clipboard.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.win32.W32APIOptions;

public interface IWindowsClipboardGdi32 extends GDI32 {
	// DEFAULT_OPTIONS is critical for W32 API functions to simplify ASCII/UNICODE
	// details
	IWindowsClipboardGdi32 INSTANCE = Native.loadLibrary("gdi32", IWindowsClipboardGdi32.class,
			W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * See microsoft documentation
	 * 
	 * Sets the bits of a DDB (Device dependent bitmap), hbmp, using color data
	 * found in specified DIB (Device independent bitmap), defined by lpvBits and
	 * lpbi.
	 * 
	 * @param hdc
	 *            - handle to device context that the DDB (device dependent bitmap)
	 *            is dependent on.
	 * @param hbmp
	 *            - handle to DDB (device dependent bitmap)
	 * @param uStartScan
	 *            - starting scan line
	 * @param cScanLines
	 *            - number of scan lines found.
	 * @param lpvBits
	 *            - pointer to DIB (device independent bitmap) color data.
	 * @param lpbi
	 *            - BITMAPINFO structure with info on DIB (device independent
	 *            bitmap)
	 * @param colorUse
	 *            - Either WinGDI.DIB_RGB_COLORS or WinGDI.DIB_PAL_COLORS
	 * @return Returns number of scan lines copied or 0 on failure.
	 */
	int SetDIBits(HDC hdc, HBITMAP hbmp, int uStartScan, int cScanLines, Pointer lpvBits, BITMAPINFO lpbi,
			int colorUse);
}
