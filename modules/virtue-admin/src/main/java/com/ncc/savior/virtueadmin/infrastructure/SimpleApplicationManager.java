package com.ncc.savior.virtueadmin.infrastructure;

import java.io.IOException;
import java.util.Set;

import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

public class SimpleApplicationManager implements IApplicationManager {

	@Override
	public void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition app) {
		try {
			SshConnectionParameters params = new SshConnectionFactory.SshConnectionParameters(vm.getHostname(),
					vm.getSshPort(), "user", "password");
			SshXpraInitiater initiator = new SshXpraInitiater(params);
			Set<Integer> displays = initiator.getXpraServers();
			int display = 45;
			if (displays.isEmpty()) {
				initiator.startXpraServer(display);
			} else {
				display = displays.iterator().next();
			}

			initiator.startXpraApp(display, app.getLaunchCommand());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
