package com.ncc.savior.desktop.xpra;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class XpraConnectionManager {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(XpraConnectionManager.class);
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<IConnectionParameters, XpraClient> activeClientsMap;
	private HashMap<Class<? extends IConnectionParameters>, IXpraInitiator.IXpraInitatorFactory> initiaterMap;
	private HashMap<IConnectionParameters, XpraApplicationManager> activeAppManagers;
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
		activeClientsMap = new HashMap<IConnectionParameters, XpraClient>();
		activeAppManagers = new HashMap<IConnectionParameters, XpraApplicationManager>();
	}

	/**
	 * Can return null if none exists yet.
	 *
	 * @param params
	 * @return
	 */

	public XpraClient getExistingClient(IConnectionParameters params) {
		return activeClientsMap.get(params);
	}

	public XpraClient createClient(IConnectionParameters params) throws IOException {
		// logger.debug("creating client with params=" + params);
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
			Set<Integer> servers = init.getXpraServers();
			// logger.debug("displays: " + servers);
			if (!servers.contains(factory.getDisplay())) {
				// logger.debug("starting Xpra Display on " + factory.getDisplay());
				init.startXpraServer(factory.getDisplay());
				// logger.debug("Display " + d + " started");
			}
		}

		XpraApplicationManager applicationManager = applicationManagerFactory.getApplicationManager(client);

		client.connect(factory, params);
		// logger.debug("Client connected with params" + params);
		activeClientsMap.put(params, client);
		activeAppManagers.put(params, applicationManager);

		return client;
	}

	public void startApplication(IConnectionParameters params, String startCommand) throws IOException {
		// logger.debug("starting application with command=" + startCommand + " params="
		// + params);
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
			Set<Integer> servers = init.getXpraServers();
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
