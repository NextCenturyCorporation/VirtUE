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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.util.SshUtil;

/**
 * Helper class that starts applications and controls Xpra over SSH.
 *
 *
 */
public class SshXpraInitiater implements IXpraInitiator {
	private static final Logger logger = LoggerFactory.getLogger(SshXpraInitiater.class);
	private static final String SUDO_OR_NOTHING = "";
	private static final String XPRA_CMD = SUDO_OR_NOTHING + " xpra";
	private static final String XPRA_STOP = XPRA_CMD + " stop";
	private static final String XPRA_START = XPRA_CMD + " start";
	// Returns list of xpra log files for started servers, but only prints the
	// number, 1 per line
	private static final String XPRA_PROBE_LIST = "cd /run/user/1000/xpra/; ls -1 :*.log | egrep -o \"[0-9]+\"";
	// version followed by a display will error if display works and is not
	// destructive (xpra list is destructive).
	private static final String XPRA_TEST_DISPLAY = XPRA_CMD + " version ";
	// if version returns successfully, it'll return a version number and this
	// should match. definitely matches 2.4.2 which is our current version.
	private static final String XPRA_VERSION_MATCH = "[0-9]+.[0-9]+.[0-9]+.*";

	private SshConnectionParameters params;

	public SshXpraInitiater(SshConnectionParameters params) {
		this.params = params;
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
		Set<Integer> displays = new TreeSet<Integer>();
		try {
			logger.debug("probing for xpra servers");
			session = getConnectedSessionWithRetries();
			channel = getConnectedChannel(XPRA_PROBE_LIST, session, null);
			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					logger.debug("found line " + line);
					int display = Integer.parseInt(line);
					displays.add(display);
				} catch (NumberFormatException e) {
					logger.error("Error parsing display from '" + line + "'");
				}
			}
			Iterator<Integer> itr = displays.iterator();
			while (itr.hasNext()) {
				Integer d = itr.next();
				if (!isDisplayReady(session, d)) {
					itr.remove();
				}
			}
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return displays;
	}

	private boolean isDisplayReady(Session session, Integer d) {
		try {
			logger.debug("attempting to probe display " + d);
			List<String> lines = SshUtil.sendCommandFromSession(session, XPRA_TEST_DISPLAY + ":" + d);
			for (String line : lines) {
				boolean matched = line.matches(XPRA_VERSION_MATCH);
				if (matched) {
					logger.debug("matched " + d);
					return true;
				}
			}
			logger.debug("Probe fails with output: " + lines);
			return false;
		} catch (JSchException | IOException e) {
			logger.error("Error testing display=" + d, e);
			return false;
		}
	}

	@Override
	public int startXpraServer(int display) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		try {
			session = getConnectedSessionWithRetries();
			session.setTimeout(10000);
			display = (display > 0 ? display : 201);
			String command = XPRA_START + " :" + display;
			command += " --systemd-run=no --pulseaudio=no --mdns=no";
			SshUtil.sendCommandFromSessionWithTimeout(session, command, 1000);
			long timeoutTime = System.currentTimeMillis() + 60000;
			while (System.currentTimeMillis() < timeoutTime) {
				if (isDisplayReady(session, display)) {
					return display;
				}
			}
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		throw new IOException("Unable to start display " + display);
	}

	@Override
	public boolean stopXpraServer(int display) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		boolean success = false;
		try {
			session = getConnectedSessionWithRetries();
			channel = getConnectedChannel(XPRA_STOP + " :" + display, session, null);
			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(":" + display + "has exited")) {
					return true;
				}
			}
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return success;
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
			String fullCommand = "export BROWSER=./savior-browser.sh; export DISPLAY=:" + display + ";"
					+ SUDO_OR_NOTHING + command;
			logger.debug("cmd: " + fullCommand);
			channel = getConnectedChannel(fullCommand, session, null);
			// InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			// BufferedReader reader = new BufferedReader(stream);
			// String line;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
		} catch (JSchException e) {
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
				logger.warn("Failed to get Xpra Servers on " + params.getHost() + ".  Tries left=" + (triesLeft)
						+ " Error=" + e.getMessage());
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
		SshXpraInitiater init = new SshXpraInitiater(p);

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
