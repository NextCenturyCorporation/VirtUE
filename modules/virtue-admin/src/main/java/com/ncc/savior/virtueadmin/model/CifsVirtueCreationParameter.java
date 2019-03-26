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
package com.ncc.savior.virtueadmin.model;

/**
 * Data Access Object (DAO) for creating and passing CIFS Proxy Virtues as
 * defined by the CIFS Proxy API.
 *
 */
public class CifsVirtueCreationParameter {
	private String name;
	private String id;
	private String username;
	private String password;

	protected CifsVirtueCreationParameter() {

	}

	public CifsVirtueCreationParameter(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public CifsVirtueCreationParameter(String name, String id, String username, String password) {
		super();
		this.name = name;
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "CifsVirtueCreationParameter [name=" + name + ", id=" + id + ", username=" + username + ", password="
				+ password + "]";
	}
}
