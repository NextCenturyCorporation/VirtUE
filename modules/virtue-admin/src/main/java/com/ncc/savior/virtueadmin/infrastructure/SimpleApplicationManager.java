package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(SimpleApplicationManager.class);
	private static Random rand = new Random();
	private String defaultPassword;
	private File defaultCertificate;

	public SimpleApplicationManager(String defaultPassword) {
		this.defaultPassword = defaultPassword;
	}

	public SimpleApplicationManager(File defaultCertificate) {
		this.defaultCertificate = defaultCertificate;
	}

	@Override
	public void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition app, int maxTries) {
		try {
			SshConnectionParameters params = null;
			if (defaultCertificate != null) {
				params = new SshConnectionFactory.SshConnectionParameters(vm.getHostname(), vm.getSshPort(),
						vm.getUserName(), defaultCertificate);
			} else {
				params = new SshConnectionFactory.SshConnectionParameters(vm.getHostname(), vm.getSshPort(),
						vm.getUserName(), defaultPassword);
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
					break;
				}
				set = initiator.getXpraServers();
				maxTries--;
				logger.debug("after start, has displays: " + set);
				int displayChecks = 3;
				while (displays.isEmpty()) {
					displays = initiator.getXpraServers();
					logger.trace("display check: " + displays);
					displayChecks--;
					if (!displays.isEmpty()) {
						logger.debug("display found: " + displays);
						break;
					}
					if (displayChecks < 1) {
						logger.debug("display check limit reached");
						break;
					}
					Thread.sleep(250);
				}
				if (set.isEmpty() && maxTries > 0) {
					logger.debug("Attempt to create display failed.  retries: " + maxTries);
					Thread.sleep(500);
				} else {
					display = set.iterator().next();
					logger.debug("Attempt to create display succeeded.  display: " + display);
					break;
				}
			}
			initiator.startXpraApp(display, app.getLaunchCommand());
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
