package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;

import org.springframework.util.SocketUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.network.JschUtils;
import com.ncc.savior.network.SshConnectionParameters;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance;
import com.ncc.savior.virtueadmin.model.desktop.WindowsApplicationInstance;
import com.ncc.savior.virtueadmin.util.SaviorException;
import com.ncc.savior.virtueadmin.util.SaviorException.ErrorCode;

public class WindowsApplicationManager implements IApplicationManager {

	private File sshCertificate;

	public WindowsApplicationManager(File sshCertificate) {
		if (sshCertificate == null || !sshCertificate.canRead()) {
			throw new IllegalArgumentException("sshCertificate is null or unreadable: " + sshCertificate);
		}
		this.sshCertificate = sshCertificate;
	}

	@Override
	public IApplicationInstance startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition application,
			int maxTries) throws IllegalArgumentException {
		if (vm.getOs() != OS.WINDOWS) {
			throw new IllegalArgumentException("vm OS must be Windows, but was: " + vm.getOs());
		}
		
		IApplicationInstance appInstance;
		appInstance = new WindowsApplicationInstance(application, vm.getHostname(), vm.getSshPort(), vm.getUserName(), vm.getPrivateKey());
		
		return appInstance;
	}

}
