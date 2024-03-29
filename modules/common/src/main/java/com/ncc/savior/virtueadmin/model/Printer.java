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


/**
 * Application Data Transfer Object (DTO).
 *
 *
 */
@Entity
public class Printer {
	@Id
	private String id;
	private String name;
	private String address;
	private String status;
	private boolean enabled;

	public Printer(String id, String name, String address, String status, boolean enabled) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.status = status;
		this.enabled = enabled;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected Printer() {

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getAddress() {
		return address;
	}

	public boolean isEnabled() {
		return enabled;
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

	public void setStatus(String status) {
		this.status = status;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "Whitelisted Network: [id=" + id + ", name=" + name + ", address=" + address + ", status=" + status
				+ ", enabled=" + enabled + "]";
	}

	/**
	 * Is an int actually big enough? #TODO
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((enabled) ? 1 : 0);
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
		Printer other = (Printer) obj;

		// check if both null or same reference, and if not, then check equals.
		if (name != other.getName() || !name.equals(other.getName())) {
			return false;
		}
		if (id != other.id || !id.equals(other.id)) {
			return false;
		}
		if (address != other.address || !address.equals(other.address)) {
			return false;
		}
		if (status != other.status || !status.equals(other.status)) {
			return false;
		}
		if (enabled != other.isEnabled()) {
			return false;
		}

		return true;
	}
	public static final Comparator<? super Printer> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	private static class CaseInsensitiveNameComparator implements Comparator<Printer> {
		@Override
		public int compare(Printer o1, Printer o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getName() + " " + o1.getAddress(), o2.getName() + " " + o2.getAddress());
		}
	}
}
