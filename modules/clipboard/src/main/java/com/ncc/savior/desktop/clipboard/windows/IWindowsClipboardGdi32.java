/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
