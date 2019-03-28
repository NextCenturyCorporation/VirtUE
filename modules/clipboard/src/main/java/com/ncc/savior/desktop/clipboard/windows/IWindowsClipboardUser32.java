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
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * Interface to give functions for the clipboard specific calls for Windows. See
 * windows API for documentation on thses methods.
 *
 * https://msdn.microsoft.com/en-us/library/windows/desktop/ms649014(v=vs.85).aspx
 */
public interface IWindowsClipboardUser32 extends User32 {
	// DEFAULT_OPTIONS is critical for W32 API functions to simplify ASCII/UNICODE
	// details
	IWindowsClipboardUser32 INSTANCE = Native.loadLibrary("user32",
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

	short GetKeyState(int vKey);
}
