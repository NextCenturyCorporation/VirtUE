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
package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtualMachine;
import com.ncc.savior.virtueadmin.template.ITemplateService;

/**
 * Simple {@link IApplicationManager} implementation that uses the
 * {@link SshXpraInitiater} to start applications on a default display.
 * 
 *
 */
public class SimpleApplicationManager implements IApplicationManager {
	private static final Logger logger = LoggerFactory.getLogger(SimpleApplicationManager.class);
	private static Random rand = new Random();
	private String defaultPassword;
	private ITemplateService templateService;

	public SimpleApplicationManager(String defaultPassword, ITemplateService templateService) {
		this(templateService);
		this.defaultPassword = defaultPassword;
	}

	public SimpleApplicationManager(ITemplateService templateService) {
		this.templateService = templateService;
	}

	@Override
	public int startOrGetXpraServer(VirtualMachine vm, File privateKeyFile) throws IOException {
		SshConnectionParameters params = SshConnectionParameters.withExistingPemFile(vm.getHostname(), vm.getSshPort(),
				vm.getUserName(), privateKeyFile);
		SshXpraInitiater initiator = new SshXpraInitiater(params, templateService);
		Set<Integer> servers = initiator.getXpraServers();
		if (servers.isEmpty()) {
			int attemptedDisplay = 100 + rand.nextInt(100);
			initiator.startXpraServer(attemptedDisplay);
			servers = initiator.getXpraServers();
		}
		if (servers.isEmpty()) {
			return -1;
		} else {
			return servers.iterator().next();
		}
	}

	@Override
	public void startApplicationOnVm(VirtualMachine vm, ApplicationDefinition app, String cliParams, int maxTries) {
		try {
			SshXpraInitiater initiator = getXpraInitiator(vm);
			int display = startAndReturnXpraServerWithRetries(maxTries, initiator);
			logger.debug("starting app");
			String cmd = app.getLaunchCommand();
			if (cliParams != null) {
				cmd += " " + cliParams;
			}
			initiator.startXpraApp(display, cmd);
		} catch (IOException | InterruptedException e) {
			String msg = "Error attempting to start application!";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, msg, e);
		}
	}

	@Override
	public int startOrGetXpraServerWithRetries(VirtualMachine vm, File privateKeyFile, int maxTries) {
		try {
			SshXpraInitiater initiator = getXpraInitiator(vm);
			return startAndReturnXpraServerWithRetries(maxTries, initiator);
		} catch (IOException | InterruptedException e) {
			String msg = "Error getting Xpra Server!";
			logger.error(msg, e);
			throw new SaviorException(SaviorErrorCode.XPRA_FAILED, msg, e);
		}
	}

	private SshXpraInitiater getXpraInitiator(VirtualMachine vm) {
		SshConnectionParameters params = null;
		if (vm.getPrivateKey() != null) {
			params = SshConnectionParameters.withPemString(vm.getHostname(), vm.getSshPort(), vm.getUserName(),
					vm.getPrivateKey());
		} else {
			params = SshConnectionParameters.withPassword(vm.getHostname(), vm.getSshPort(), vm.getUserName(),
					defaultPassword);
		}
		SshXpraInitiater initiator = new SshXpraInitiater(params, templateService);
		return initiator;
	}

	private int startAndReturnXpraServerWithRetries(int maxTries, SshXpraInitiater initiator)
			throws IOException, InterruptedException {
		Set<Integer> set = null;
		int display = -1;
		while (true) {
			Set<Integer> displays = initiator.getXpraServersWithRetries();
			if (displays.isEmpty()) {
				try {
					int attemptedDisplay = 100 + rand.nextInt(100);
					logger.debug("attempting to start server on display " + attemptedDisplay);
					return initiator.startXpraServer(attemptedDisplay);
				} catch (IOException e) {
					// Expecting to return with a display.  However, if it fails, we will just try again.
				}
			} else {
				display = displays.iterator().next();
				logger.debug("using display " + display);
				break;
			}
			set = initiator.getXpraServersWithRetries();
			maxTries--;
			logger.debug("after start, has displays: " + set);
			int displayChecks = 3;
			while (displays.isEmpty()) {
				displays = initiator.getXpraServersWithRetries();
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
				Thread.sleep(1000);
			}
			if (set.isEmpty() && maxTries > 0) {
				logger.debug("Attempt to create display failed.  retries: " + maxTries);
				Thread.sleep(2000);
			} else if (set.isEmpty()) {
				// no tries left but no displays
				String msg = "Unable to create Xpra server.";
				logger.error(msg);
				throw new SaviorException(SaviorErrorCode.XPRA_FAILED, msg);
			} else {
				display = set.iterator().next();
				logger.debug("Attempt to create display succeeded.  display: " + display);
				break;
			}
			logger.debug("going to top of loop");
		}
		return display;
	}

}
