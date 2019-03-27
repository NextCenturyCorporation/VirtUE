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
package com.ncc.savior.desktop.sidebar.prefs;

/**
 * Contains path, key and value for preference. This data is generally not user
 * friendly, but the values used by the Desktop Application to use the
 * preferences.
 *
 */
public class DesktopPreferenceData {
	public String pathFromUserRoot;
	public String key;
	public String value;

	public DesktopPreferenceData(String pathFromUserRoot, String key, String value) {
		super();
		this.pathFromUserRoot = pathFromUserRoot;
		this.key = key;
		this.value = value;
		if (pathFromUserRoot.indexOf("/") == 0) {
			this.pathFromUserRoot = pathFromUserRoot.substring(1);
		}
	}

	public String getPathFromUserRoot() {
		return pathFromUserRoot;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}