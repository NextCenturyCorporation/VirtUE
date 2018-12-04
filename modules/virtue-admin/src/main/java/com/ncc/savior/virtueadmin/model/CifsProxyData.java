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