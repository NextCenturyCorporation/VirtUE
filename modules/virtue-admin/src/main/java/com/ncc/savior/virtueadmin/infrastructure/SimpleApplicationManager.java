package com.ncc.savior.virtueadmin.infrastructure;

import java.io.IOException;
import java.util.Set;

import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;

/**
 * Simple {@link IApplicationManager} implementation that uses the
 * {@link SshXpraInitiater} to start applications on a default display.
 * 
 *
 */
// TODO credentials need to be added somewhere.
public class SimpleApplicationManager implements IApplicationManager {

	private static final String DEFAULT_PASSWORD = "password";
	private static final String DEFAULT_USER = "user";
	private static final int DEFAULT_DISPLAY = 45;

	@Override
	public void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition app) {
		try {
			SshConnectionParameters params = new SshConnectionFactory.SshConnectionParameters(vm.getHostname(),
					vm.getSshPort(), DEFAULT_USER, DEFAULT_PASSWORD);
			SshXpraInitiater initiator = new SshXpraInitiater(params);
			Set<Integer> displays = initiator.getXpraServers();
			int display = DEFAULT_DISPLAY;
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
