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
import com.ncc.savior.desktop.clipboard.windows.WindowsClipboardWrapper;
import com.sun.jna.Pointer;

/**
 * Base class for clipboard data.
 *
 */
public abstract class ClipboardData implements Serializable {
	private static final long serialVersionUID = 1L;
	private ClipboardFormat format;
	private boolean isCacheable;

	protected ClipboardData(ClipboardFormat format) {
		this.format = format;
		this.isCacheable = false;
	}

	/**
	 * Gets the format of the clipboard. Currently this is windows values, but once
	 * we incorporate linux, we'll need some generic class or something that can
	 * convert between OSs.
	 *
	 * @return
	 */
	public ClipboardFormat getFormat() {
		return format;
	}

	/**
	 * Gets the memory pointer for the data on a windows machine. This memory will
	 * NOT be cleared by JNA. It is assumed the Windows clipboard will clear the
	 * data. However, if this data is pass anywhere else, it needs to be cleared
	 * manually.
	 *
	 * @return
	 */
	public abstract Pointer createWindowsData(WindowsClipboardWrapper wrapper);

	/**
	 * Gets the memory pointer for the data on a linux machine. This memory will be
	 * cleared by JNA.
	 * 
	 * @return
	 */
	public abstract Pointer createLinuxData();

	/**
	 * Gets the number of entries in Linux data. Entries can be either 8, 16, or 32
	 * bits each (1, 2, or 4 bytes). See {@link #getLinuxEntrySizeBits()} for the
	 * size of the entry.
	 * 
	 * @return
	 */
	public abstract int getLinuxNumEntries();

	/**
	 * must be 8, 16, or 32
	 *
	 * @return
	 */
	public abstract int getLinuxEntrySizeBits();

	/**
	 * Returns whether the OS (the paster) using this data (not the creator/copier)
	 * can cache the data without rechecking. Typically, data copied from a Windows
	 * machine can be cached because Windows will notify on clipboard changes. Data
	 * that originated on a Linux machine cannot be cached becuase Linux does not
	 * update on clipboard changes.
	 * 
	 * @return
	 */
	public boolean isCacheable() {
		return isCacheable;
	}

	/**
	 * Length of the data in bytes from {@link #createWindowsData()}
	 * 
	 * @return
	 */
	public abstract long getWindowsDataLengthBytes();
}
