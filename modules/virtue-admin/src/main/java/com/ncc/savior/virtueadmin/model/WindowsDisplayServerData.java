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
public class WindowsDisplayServerData {
	@Id
	private String windowsApplicationVmId;
	private String username;
	@OneToOne
	private VirtualMachine wdsVm;

	/**
	 * for jpa.
	 */
	protected WindowsDisplayServerData() {

	}

	public WindowsDisplayServerData(String username, String windowsApplicationVmId, VirtualMachine wdsVm) {
		super();
		this.windowsApplicationVmId = windowsApplicationVmId;
		this.username = username;
		this.wdsVm = wdsVm;
	}

	public String getWindowsApplicationVmId() {
		return windowsApplicationVmId;
	}

	public void setWindowsApplicationVmId(String windowsApplicationVmId) {
		this.windowsApplicationVmId = windowsApplicationVmId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public VirtualMachine getWdsVm() {
		return wdsVm;
	}

	public void setWdsVm(VirtualMachine wdsVm) {
		this.wdsVm = wdsVm;
	}

	@Override
	public String toString() {
		return "WindowsDisplayServerData [windowsApplicationVmId=" + windowsApplicationVmId + ", username=" + username
				+ ", wdsVm=" + wdsVm + "]";
	}
}