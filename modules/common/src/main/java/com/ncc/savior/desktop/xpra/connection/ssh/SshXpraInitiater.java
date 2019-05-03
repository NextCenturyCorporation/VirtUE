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
package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.SshUtil;
import com.ncc.savior.util.SshUtil.SshResult;
import com.ncc.savior.virtueadmin.template.FreeMarkerTemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService;
import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;

/**
 * Helper class that starts applications and controls Xpra over SSH.
 *
 *
 */
public class SshXpraInitiater implements IXpraInitiator {
	private static final Logger logger = LoggerFactory.getLogger(SshXpraInitiater.class);
	/**
	 * The script for getting active xpra session numbers. Note: Used by both
	 * Desktop and Virtue Admin Server.
	 */
	private static final String XPRA_PROBE_TEMPLATE = "xpra-probe.tpl";
	private static final String XPRA_TEST_DISPLAY_COMMAND = "version";
	/**
	 * The script for running a xpra command (e.g., "xpra version"). Note: Used by
	 * both Desktop and Virtue Admin Server.
	 */
	private static final String XPRA_COMMAND_TEMPLATE = "xpra-command.tpl";
	private static final String XPRA_START_TEMPLATE = "xpra-start.tpl";
	private static final String XPRA_STOP_COMMAND = "stop";
	private static final String START_XPRA_APP_TEMPLATE = "start-xpra-app.tpl";

	private SshConnectionParameters params;
	private final ITemplateService templateService;

	public SshXpraInitiater(SshConnectionParameters params, ITemplateService templateService) {
		this.params = params;
		this.templateService = templateService;
	}

	@Override
	public Set<Integer> getXpraServersWithRetries() throws IOException {
		int triesLeft = 3;
		IOException lastException = null;
		while (triesLeft > 0) {
			try {
				triesLeft--;
				Set<Integer> servers = getXpraServers();
				return servers;
			} catch (IOException e) {
				lastException = e;
				logger.warn("Failed to get Xpra Servers.  Tries left=" + (triesLeft) + " Error=" + e.getMessage());
			}
		}
		throw lastException;
	}

	public Set<Integer> getXpraServers() throws IOException {
		Session session = null;
		ChannelExec channel = null;
		Set<Integer> displays = null;
		try {
			logger.debug("probing for xpra servers");
			session = getConnectedSessionWithRetries();
			List<String> lines = SshUtil.runCommandsFromFile(templateService, session, XPRA_PROBE_TEMPLATE,
					Collections.emptyMap());
			Session finalSession = session;
			displays = lines.stream().map(line -> {
				try {
					return Integer.parseInt(line);
				} catch (NumberFormatException e) {
					return null;
				}
			}).filter(display -> display != null && isDisplayReady(finalSession, display)).collect(Collectors.toSet());
			logger.debug("found xpra servers: " + displays);
		} catch (JSchException | TemplateException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return displays;
	}

	private boolean isDisplayReady(Session session, int d) {
		try {
			logger.debug("attempting to probe display " + d);
			// This command is strangely slow. Informal tests showed times up to 1300ms on local laptop, 3300ms on Xen domU.
			SshResult sshResult = sendXpraCommand(session, XPRA_TEST_DISPLAY_COMMAND, d, 5000);
			boolean ready = sshResult.getExitStatus() == 0;
			logger.debug("display " + d + " ready? " + ready);
			return ready;
		} catch (JSchException | IOException | TemplateException e) {
			logger.error("Error testing display=" + d, e);
			return false;
		}
	}

	// NOTE: this does not quote the options (not needed so far)
	private SshResult sendXpraCommand(Session session, String xpraCommand, int display, int timeoutMs,
			String... options) throws TemplateException, JSchException, IOException {
		Map<String, Object> dataModel = new HashMap<String, Object>();
		dataModel.put("command", xpraCommand);
		dataModel.put("display", ":" + display);
		dataModel.put("options", String.join(" ", options));
		SshResult sshResult = SshUtil.runTemplateFile(templateService, session, XPRA_COMMAND_TEMPLATE, dataModel,
				timeoutMs);
		if (sshResult.getExitStatus() != 0 && logger.isDebugEnabled()) {
			logger.debug("exit status = " + sshResult.getExitStatus());
			logger.debug("stdout: " + sshResult.getOutput());
			logger.debug("stderr: " + sshResult.getError());
		}
		return sshResult;
	}

	@Override
	public int startXpraServer(int display) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		try {
			session = getConnectedSessionWithRetries();
			session.setTimeout(10000);
			display = (display > 0 ? display : 201);
			Map<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put("display", ":" + display);
			dataModel.put("options", "");
			SshResult sshResult = SshUtil.runTemplateFile(templateService, session, XPRA_START_TEMPLATE, dataModel,
					5000);
			logger.debug("exit status = {}", sshResult.getExitStatus());
			if (sshResult.getExitStatus() != 0 && logger.isDebugEnabled()) {
				logger.debug("stdout: " + sshResult.getOutput());
				logger.debug("stderr: " + sshResult.getError());
			}

			long timeoutTime = System.currentTimeMillis() + 60000;
			while (System.currentTimeMillis() < timeoutTime) {
				if (isDisplayReady(session, display)) {
					return display;
				}
			}
		} catch (JSchException | TemplateException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		throw new IOException("Unable to start display " + display);
	}

	@Override
	public boolean stopXpraServer(int display) throws IOException {
		Session session = null;
		try {
			session = getConnectedSessionWithRetries();
			SshResult sshResult = sendXpraCommand(session, XPRA_STOP_COMMAND, display, 500);
			return sshResult.getExitStatus() == 0;
		} catch (JSchException | TemplateException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, null);
		}
	}

	@Override
	public void startXpraApp(int display, String command) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		if (display < 0) {
			logger.warn("unable to start application due to no display!");
			return;
		}
		try {
			session = getConnectedSessionWithRetries();
			StringBuilderWriter out = new StringBuilderWriter();
			Map<String, Object> dataModel = new HashMap<>();
			dataModel.put("display", display);
			dataModel.put("command", command);
			templateService.processTemplate(START_XPRA_APP_TEMPLATE, out, dataModel);
			String fullCommand = out.toString();
			logger.debug("cmd: " + fullCommand);
			channel = getConnectedChannel(fullCommand, session, null);
			// InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			// BufferedReader reader = new BufferedReader(stream);
			// String line;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
		} catch (JSchException | TemplateException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
	}

	@Override
	public void stopAllXpraServers() throws IOException {
		Set<Integer> displays = getXpraServersWithRetries();
		for (Integer display : displays) {
			stopXpraServer(display);
		}
	}

	private ChannelExec getConnectedChannel(String command, Session session, OutputStream out) throws JSchException {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		channel.setErrStream(System.err);

		channel.connect();
		return channel;
	}

	private Session getConnectedSessionWithRetries() throws JSchException {
		int triesLeft = 3;
		JSchException lastException = null;
		while (triesLeft > 0) {
			triesLeft--;
			try {
				Session session = JschUtils.getUnconnectedSession(params);
				session.connect();
				return session;
			} catch (JSchException e) {
				lastException = e;
				logger.warn("Failed to connect to " + params.getHost() + ".  Tries left=" + (triesLeft) + " Error="
						+ e.getMessage());
				logger.debug("params=" + params);
			}
		}
		throw lastException;
	}

	private void closeAll(Session session, ChannelExec channel) {
		if (channel != null) {
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}

	public static final void main(String[] args) throws IOException {
		SshConnectionParameters p = SshConnectionParameters.withPassword("localhost", 22, "user", "password");
		SshXpraInitiater init = new SshXpraInitiater(p, new FreeMarkerTemplateService("templates"));

		Set<Integer> displays = init.getXpraServersWithRetries();
		System.out.println(displays);
		//
		init.stopAllXpraServers();
		//
		// displays = init.getXpraServers();
		// System.out.println(displays);

		int display = init.startXpraServer(8);
		System.out.println("started on " + display);

		displays = init.getXpraServersWithRetries();
		System.out.println(displays);

		init.startXpraApp(display, "gedit");

	}

}
