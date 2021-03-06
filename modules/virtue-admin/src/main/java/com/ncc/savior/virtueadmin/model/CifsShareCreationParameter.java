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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Data Access Object (DAO) for creating and passing CIFS Proxy Shares as
 * defined by the CIFS Proxy API.
 *
 */

@Entity
public class CifsShareCreationParameter {
	@Id
	@GeneratedValue
	@Column(name = "id", updatable = false, nullable = false)
	private long id;
	private String name;
	private String virtueId;
	private String server;
	private String path;
	@ElementCollection
	private List<String> permissions;
	private String type;
	private String exportedName;

	public CifsShareCreationParameter() {
	}

	public CifsShareCreationParameter(String name, String virtueId, String server, String path,
			List<String> permissions, String type) {
		super();
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
		this.path = path;
		this.permissions = permissions;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public void setVirtueId(String virtueId) {
		this.virtueId = virtueId;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExportedName() {
		return exportedName;
	}

	public void setExportedName(String exportedName) {
		this.exportedName = exportedName;
	}

	public long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "CifsShareCreationParameter [name=" + name + ", virtueId=" + virtueId + ", server=" + server + ", path="
				+ path + ", permissions=" + permissions + ", type=" + type + ", exportedName=" + exportedName + "]";
	}
}
