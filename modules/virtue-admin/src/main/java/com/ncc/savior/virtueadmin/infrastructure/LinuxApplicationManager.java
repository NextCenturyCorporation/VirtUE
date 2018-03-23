package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.network.SshConnectionParameters;
import com.ncc.savior.virtueadmin.model.AbstractVirtualMachine;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.IApplicationInstance;
import com.ncc.savior.virtueadmin.model.desktop.LinuxApplicationInstance;

/**
 * Simple {@link IApplicationManager} implementation that uses the
 * {@link SshXpraInitiater} to start applications on a default display.
 * 
 *
 */
// TODO credentials need to be added somewhere.
public class LinuxApplicationManager implements IApplicationManager {
	private static final Logger logger = LoggerFactory.getLogger(LinuxApplicationManager.class);
	private static Random rand = new Random();
	private String defaultPassword;
	private File defaultCertificate;

	public LinuxApplicationManager(String defaultPassword) {
		this.defaultPassword = defaultPassword;
	}

	public LinuxApplicationManager(File defaultCertificate) {
		this.defaultCertificate = defaultCertificate;
	}

	@Override
	public IApplicationInstance startApplicationOnVm(AbstractVirtualMachine vm, ApplicationDefinition app, int maxTries) {
		IApplicationInstance appInstance;
		try {
			SshConnectionParameters params = null;
			if (defaultCertificate != null) {
				params = new SshConnectionParameters(vm.getHostname(), vm.getSshPort(), vm.getUserName(),
						defaultCertificate);
			} else {
				params = new SshConnectionParameters(vm.getHostname(), vm.getSshPort(), vm.getUserName(),
						defaultPassword);
			}
			SshXpraInitiater initiator = new SshXpraInitiater(params);
			Set<Integer> set = null;
			int display = -1;
			while (true) {
				Set<Integer> displays = initiator.getXpraServers();
				if (displays.isEmpty()) {
					int attemptedDisplay = 100 + rand.nextInt(100);
					logger.debug("attempting to start server on display " + attemptedDisplay);
					initiator.startXpraServer(attemptedDisplay);
				} else {
					display = displays.iterator().next();
					logger.debug("using display " + display);
				}
				set = initiator.getXpraServers();
				maxTries--;
				logger.debug("after start, has displays: " + set);
				int displayChecks = 5;
				while (displays.isEmpty()) {
					displays = initiator.getXpraServers();
					displayChecks--;
					if (displayChecks < 1) {
						break;
					}
					Thread.sleep(500);
				}
				if (set.isEmpty() && maxTries > 0) {
					Thread.sleep(500);
				} else {
					display = set.iterator().next();
					break;
				}
			}
			initiator.startXpraApp(display, app.getLaunchCommand());
			appInstance = new LinuxApplicationInstance(app, vm.getHostname(), vm.getSshPort(),
					vm.getUserName(), vm.getPrivateKey());

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			appInstance = null;
		}
		return appInstance;
	}

}
