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
 * More User friendly description and details for a given preference. Does not
 * contain values or require querying actual preference backing store to
 * retrieve this data.
 *
 *
 */
public class DesktopPreferenceDetails {
	private String root;
	private boolean nodeCollection;
	private String description;
	private String name;
	private boolean displayInPrefTable;

	public DesktopPreferenceDetails(String name, String description, String root, boolean displayInPrefTable,
			boolean nodeCollection) {
		super();
		this.root = root;
		this.nodeCollection = nodeCollection;
		this.description = description;
		this.name = name;
		this.displayInPrefTable = displayInPrefTable;
	}

	public DesktopPreferenceDetails(String name, String description, String root, boolean displayInPrefTable) {
		this(name, description, root, displayInPrefTable, false);
	}

	public String getRoot() {
		return root;
	}

	public boolean isNodeCollection() {
		return nodeCollection;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public boolean isDisplayInPrefTable() {
		return displayInPrefTable;
	}

}
