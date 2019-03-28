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
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.win32.W32APIOptions;

/**
 * Interface to give functions for the clipboard specific calls for Windows. See
 * windows API for documentation on thses methods.
 *
 * https://docs.microsoft.com/en-us/windows/desktop/api/shellapi/nf-shellapi-dragqueryfilea
 */
public interface IWindowsClipboardShell32 extends Shell32 {
	// DEFAULT_OPTIONS is critical for W32 API functions to simplify ASCII/UNICODE
	// details
	IWindowsClipboardShell32 INSTANCE = Native.loadLibrary("shell32", IWindowsClipboardShell32.class,
			W32APIOptions.DEFAULT_OPTIONS);

	/**
	 * Use as the index into {@link #DragQueryFileA(Pointer, int, Pointer, int)} to
	 * indicate that the function should return the number of files dropped instead
	 * of its normal operation.
	 */
	static final int DRAG_QUERY_GET_NUM_FILES_INDEX = 0xFFFFFFFF;

	/**
	 * UINT DragQueryFileA( HDROP hDrop, UINT iFile, LPSTR lpszFile, UINT cch );
	 * 
	 * Retrieves the names of dropped files that result from a successful
	 * drag-and-drop operation.
	 * 
	 * @param hDrop
	 *            - Identifier of the structure that contains the file names of the
	 *            dropped files. Structure appears to be undocumented and only used
	 *            to pass to this function.
	 * @param iFile
	 *            - Index of the file to query. If the value of this parameter is
	 *            0xFFFFFFFF, DragQueryFile returns a count of the files dropped. If
	 *            the value of this parameter is between zero and the total number
	 *            of files dropped, DragQueryFile copies the file name with the
	 *            corresponding value to the buffer pointed to by the lpszFile
	 *            parameter.
	 * @param lpszFile
	 *            - The address of a buffer that receives the file name of a dropped
	 *            file when the function returns. This file name is a
	 *            null-terminated string. If this parameter is NULL, DragQueryFile
	 *            returns the required size, in characters, of this buffer.
	 * @param cch
	 *            - The size, in characters, of the lpszFile buffer.
	 * @return - A nonzero value indicates a successful call.
	 * 
	 *         When the function copies a file name to the buffer, the return value
	 *         is a count of the characters copied, not including the terminating
	 *         null character.
	 * 
	 *         If the index value is 0xFFFFFFFF, the return value is a count of the
	 *         dropped files. Note that the index variable itself returns unchanged,
	 *         and therefore remains 0xFFFFFFFF.
	 * 
	 *         If the index value is between zero and the total number of dropped
	 *         files, and the lpszFile buffer address is NULL, the return value is
	 *         the required size, in characters, of the buffer, not including the
	 *         terminating null character.
	 */
	int DragQueryFileA(Pointer hDrop, int iFile, Pointer lpszFile, int cch);
}
