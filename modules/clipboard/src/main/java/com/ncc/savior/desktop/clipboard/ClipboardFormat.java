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
package com.ncc.savior.desktop.clipboard;

import java.util.HashMap;
import java.util.Map;

public enum ClipboardFormat {
	TEXT(1, "STRING"), UNICODE(13, "UTF8_STRING"), FILES(15, "x-special/gnome-copied-files"), BITMAP(2, "image/png");

	private static Map<Integer, ClipboardFormat> windowsToLinux;
	private static Map<String, ClipboardFormat> linuxToWindows;

	static {
		windowsToLinux = new HashMap<Integer, ClipboardFormat>();
		linuxToWindows = new HashMap<String, ClipboardFormat>();
		for (ClipboardFormat v : ClipboardFormat.values()) {
			windowsToLinux.put(v.windows, v);
			linuxToWindows.put(v.linux, v);
		}
	}

	private String linux;
	private int windows;

	ClipboardFormat(int windows, String linux) {
		this.windows = windows;
		this.linux = linux;
	}

	public int getWindows() {
		return windows;
	}

	public String getLinux() {
		return linux;
	}

	public static ClipboardFormat fromWindows(int intValue) {
		return windowsToLinux.get(intValue);
	}

	public static ClipboardFormat fromLinux(String str) {
		return linuxToWindows.get(str);
	}
}
