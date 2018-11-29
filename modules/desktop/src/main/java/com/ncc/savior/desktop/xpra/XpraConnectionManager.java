package com.ncc.savior.desktop.xpra;

import java.io.IOException;
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
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class XpraConnectionManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XpraConnectionManager.class);
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<String, XpraClient> activeClientsMap;
	private HashMap<Class<? extends IConnectionParameters>, IXpraInitiator.IXpraInitatorFactory> initiaterMap;
	private HashMap<String, XpraApplicationManager> activeAppManagers;
	private IApplicationManagerFactory applicationManagerFactory;

	public XpraConnectionManager(IApplicationManagerFactory appManagerFactory) {
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
	public XpraClient createClient(IConnectionParameters params, RgbColor color, DesktopVirtue virtue)
			throws IOException {
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		// TODO DELETE ME
		// File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
		// dir = new File(dir, virtue.getId());
		// dir.mkdirs();
		// DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
		// client.addPacketListener(debugHandler);
		// client.addPacketSendListener(debugHandler);
		// TODO End debug
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
			// logger.debug("getting servers");
			Set<Integer> servers = init.getXpraServersWithRetries();
			// logger.debug("displays: " + servers);
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
		// logger.debug("starting application with command=" + startCommand + " params="
		// + params);
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
			Set<Integer> servers = init.getXpraServersWithRetries();
			// logger.debug("displays: " + servers);
			int display;
			if (!servers.isEmpty()) {
				display = servers.iterator().next();
				// logger.debug("starting application on display=" + display + " command=" +
				// startCommand);
				init.startXpraApp(display, startCommand);
			} else {
				throw new IOException("Error getting starting and getting display from Xpra.");
			}
		}
	}

}
