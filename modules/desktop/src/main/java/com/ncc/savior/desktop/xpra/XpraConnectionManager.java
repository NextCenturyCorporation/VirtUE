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
package com.ncc.savior.desktop.xpra;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.alerting.VirtueAlertMessage;
import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator.IXpraInitatorFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory.TcpConnectionParameters;
import com.ncc.savior.desktop.xpra.debug.DebugPacketHandler;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class XpraConnectionManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XpraConnectionManager.class);
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<String, XpraClient> activeClientsMap;
	private HashMap<Class<? extends IConnectionParameters>, IXpraInitiator.IXpraInitatorFactory> initiaterMap;
	private HashMap<String, XpraApplicationManager> activeAppManagers;
	private IApplicationManagerFactory applicationManagerFactory;
	private boolean packetDebug;

	public XpraConnectionManager(IApplicationManagerFactory appManagerFactory, boolean packetDebug) {
		this.applicationManagerFactory = appManagerFactory;
		connectionFactoryMap = new HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory>();
		// Set values by config?
		connectionFactoryMap.put(TcpConnectionParameters.class, new TcpConnectionFactory());
		connectionFactoryMap.put(SshConnectionParameters.class, new SshConnectionFactory());
		initiaterMap = new HashMap<Class<? extends IConnectionParameters>, IXpraInitiator.IXpraInitatorFactory>();

		initiaterMap.put(SshConnectionParameters.class, new IXpraInitiator.IXpraInitatorFactory() {

			@Override
			public IXpraInitiator getXpraInitiator(IConnectionParameters params) {
				if (params instanceof SshConnectionParameters) {
					return new SshXpraInitiater((SshConnectionParameters) params);
				}
				throw new IllegalArgumentException(
						"SshXpraInitiator requires " + SshConnectionParameters.class.getCanonicalName() + ".  Got "
								+ params.getClass().getCanonicalName());
			}
		});
		activeClientsMap = new HashMap<String, XpraClient>();
		activeAppManagers = new HashMap<String, XpraApplicationManager>();
		this.packetDebug = packetDebug;
	}

	/**
	 * Can return null if none exists yet.
	 *
	 * @param params
	 * @return
	 */

	public XpraClient getExistingClient(IConnectionParameters params) {
		return activeClientsMap.get(params.getConnectionKey());
	}

	/**
	 * needs display in params
	 *
	 * @param params
	 * @param color
	 * @param virtue
	 * @return
	 * @throws IOException
	 */
	public XpraClient createClient(IConnectionParameters params, RgbColor color, DesktopVirtue virtue) {
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		if (packetDebug) {
			File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
			dir = new File(dir, virtue.getId());
			dir.mkdirs();
			DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
			client.addPacketListener(debugHandler);
			client.addPacketSendListener(debugHandler);
		}
		XpraApplicationManager applicationManager = applicationManagerFactory.getApplicationManager(client, color);
		client.setErrorCallback((msg, e) -> {
			applicationManager.closeAllWindows();
			VirtueAlertMessage pam = new VirtueAlertMessage("Virtue connection failed", virtue.getId(),
					virtue.getName(), "Connection to virtue closed unexpectedly.  " + msg + e.getLocalizedMessage());
			UserAlertingServiceHolder.sendAlertLogError(pam, logger);
		});
		client.connect(factory, params);
		client.setDisplay(params.getDisplay());

		// logger.debug("Client connected with params" + params);
		activeClientsMap.put(params.getConnectionKey(), client);
		activeAppManagers.put(params.getConnectionKey(), applicationManager);

		return client;
	}

	public void createXpraServerAndAddDisplayToParams(IConnectionParameters params) throws IOException {
		// logger.debug("creating client with params=" + params);

		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
			logger.debug("getting xpra servers");
			Set<Integer> servers = init.getXpraServersWithRetries();
			logger.debug("xpra displays: " + servers);
			if (servers.size() >= 1) {
				params.setDisplay(servers.iterator().next());
			}
			// if (!servers.contains(factory.getDisplay())) {
			// logger.error("Server does not have expected display running on XPRA.
			// Display=" + factory.getDisplay());
			// // logger.debug("starting Xpra Display on " + factory.getDisplay());
			// int d = init.startXpraServer(factory.getDisplay());
			// // logger.debug("Display " + d + " started");
			// }
		}
	}

	public void startApplication(IConnectionParameters params, String startCommand) throws IOException {
		logger.debug("starting application with command=" + startCommand + " params=" + params);
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
			Set<Integer> servers = init.getXpraServersWithRetries();
			// logger.debug("displays: " + servers);
			int display;
			if (!servers.isEmpty()) {
				display = servers.iterator().next();
				logger.debug("starting application on display=" + display + " command=" + startCommand);
				init.startXpraApp(display, startCommand);
			} else {
				throw new IOException("Error getting starting and getting display from Xpra.");
			}
		}
	}

	public void closeActiveClients() {
		Collection<XpraClient> clients = activeClientsMap.values();

		for (XpraClient client : clients) {
			client.close();
		}

		activeClientsMap.clear();
	}

}
