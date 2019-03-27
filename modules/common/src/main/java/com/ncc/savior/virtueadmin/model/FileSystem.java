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

import java.util.Comparator;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonGetter;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *
 */
@Entity
@Schema(description="Represents an external file system, usually a windows share, that can be attached to the virtual machines of a virtue.")
public class FileSystem {
	@Id
	@Schema(description="ID of the given file system.")
	protected String id;
	@Schema(description="Full path to the share.  Typically in the format '\\\\hostname\\path\\to\\share'.")
	protected String address;
	@Schema(description="Human readable name of the share.")
	protected String name;
	@Schema(description="Toggle to whether the share is enabled or not.  May not be implemented.")
	protected boolean enabled;
	@Schema(description="True/false for read permission to the share.  May not be implemented.")
	protected boolean readPerm;
	@Schema(description="True/false for write permission to the share.  May not be implemented.")
	protected boolean writePerm;
	@Schema(description="True/false for execute permission to the share.  May not be implemented.")
	protected boolean executePerm;

	public FileSystem(String id, String name, String address, boolean enabled,
										boolean readPerm, boolean writePerm, boolean executePerm) {
		super();
		this.id = id;
		this.address = address;
		this.name = name;
		this.enabled = enabled;
		this.readPerm = readPerm;
		this.writePerm = writePerm;
		this.executePerm = executePerm;
	}

	/**
	 * Used for jackson deserialization
	 */
 	public FileSystem() {
		this.id = "id_65536";
		this.name = "name_65536"; // easily searchable value, just for debugging
		this.address = "address_65536";
		this.enabled = true;
		this.readPerm = false;
		this.writePerm = true;
		this.executePerm = false;
	}

	public FileSystem(String templateId, FileSystem fileSys) {
		this.id = templateId;
		this.name = fileSys.getName();
		this.address = fileSys.getAddress();
		this.enabled = fileSys.isEnabled();
		this.readPerm = fileSys.getReadPerm();
		this.writePerm = fileSys.getWritePerm();
		this.executePerm = fileSys.getExecutePerm();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	@JsonGetter
	public boolean isEnabled() {
		return enabled;
	}

	@JsonGetter
	public boolean getReadPerm() {
		return readPerm;
	}

	@JsonGetter
	public boolean getWritePerm() {
		return writePerm;
	}

	@JsonGetter
	public boolean getExecutePerm() {
		return executePerm;
	}

	// below setters used for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setEnabled(boolean newStatus) {
		this.enabled = newStatus;
	}

	public void setReadPerm(boolean read) {
		this.readPerm = read;
	}

	public void setWritePerm(boolean write) {
		this.writePerm = write;
	}

	public void setExecutePerm(boolean execute) {
		this.executePerm = execute;
	}

	@Override
	public String toString() {
		return "FileSystem [id=" + id + ", name=" + name + ", address=" + address + ", enabled=" + enabled +
		", readPerm=" + readPerm + ", writePerm=" + writePerm + ", executePerm=" + executePerm + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		FileSystem other = (FileSystem) obj;

		// check if both null or same reference, and if not, then check equals.
		if (id == null || !id.equals(other.id)) {
			return false;
		}
		if (name == null || !name.equals(other.name)) {
			return false;
		}
		if (address == null || !address.equals(other.address)) {
			return false;
		}

		return true;
	}
	public static final Comparator<? super FileSystem> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	protected static class CaseInsensitiveNameComparator implements Comparator<FileSystem> {
		@Override
		public int compare(FileSystem o1, FileSystem o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
		}
	}
}
