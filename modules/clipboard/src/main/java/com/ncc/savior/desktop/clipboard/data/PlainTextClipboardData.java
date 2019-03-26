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
package com.ncc.savior.desktop.clipboard.data;

import java.io.Serializable;

import com.ncc.savior.desktop.clipboard.ClipboardFormat;
import com.ncc.savior.desktop.clipboard.windows.NativelyDeallocatedMemory;
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

/**
 * Specific implementation for simple plain text clipboard data. This format is
 * for standard ASCI text.
 *
 */

public class PlainTextClipboardData extends ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private String data;

	public PlainTextClipboardData(String data) {
		super(ClipboardFormat.TEXT);
		this.data = data;
	}

	@Override
	public Pointer createWindowsData(WindowsClipboardWrapper wrapper) {
		Memory winMemory = new NativelyDeallocatedMemory(getWindowsDataLengthBytes());
		winMemory.clear();
		winMemory.setString(0, data);
		return winMemory;
	}

	@Override
	public String toString() {
		return "PlainTextClipboardData [data=" + data + "]";
	}

	@Override
	public Pointer createLinuxData() {
		int size = 1 * (data.length());
		Memory mem = new Memory(size + 1);
		mem.clear();
		mem.setString(0, data);
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
		return 1 * (data.getBytes().length + 1);
	}

}
