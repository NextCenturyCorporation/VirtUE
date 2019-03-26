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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class CifsProxyData {
	@Id
	private String username;
	@OneToOne
	private VirtueUser user;
	@OneToOne
	private VirtualMachine cifsVm;
	private long timeoutMillis;

	/**
	 * for jpa.
	 */
	protected CifsProxyData() {

	}

	public CifsProxyData(VirtueUser user, VirtualMachine cifsVm, long timeoutMillis) {
		super();
		setUser(user);

		this.cifsVm = cifsVm;
		this.timeoutMillis = timeoutMillis;
	}

	public VirtueUser getUser() {
		return user;
	}

	public void setUser(VirtueUser user) {
		this.user = user;
		if (user != null) {
			this.username = user.getUsername();
		}
	}

	public VirtualMachine getCifsVm() {
		return cifsVm;
	}

	public void setCifsVm(VirtualMachine cifsVm) {
		this.cifsVm = cifsVm;
	}

	public long getTimeoutMillis() {
		return timeoutMillis;
	}

	public void setTimeoutMillis(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}
}