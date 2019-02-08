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