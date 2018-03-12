package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

public class WindowsApplicationInstance extends BaseApplicationInstance {
	protected WindowsApplicationInstance() {
	}

	public WindowsApplicationInstance(ApplicationDefinition application, String hostname, int sshPort, String userName,
			String privateKey) {
		super(application, hostname, sshPort, userName, privateKey);
	}

	@Override
	public String toString() {
		return "DesktopVirtueApplication [application=" + getApplicationDefinition()
				+ ", hostname=" + hostname + ", port=" + port + ", userName=" + userName + "]";
	}
}
