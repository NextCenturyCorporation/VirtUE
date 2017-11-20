package com.ncc.savior.desktop.xpra;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManager;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator.IXpraInitatorFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.ssh.SshXpraInitiater;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory.TcpConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;

//TODO review JavaFX specific code here.
public class XpraConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(XpraConnectionManager.class);
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<IConnectionParameters, XpraClient> activeClientsMap;
	private JavaFxKeyboard keyboard;
	private HashMap<Class<? extends IConnectionParameters>, IXpraInitiator.IXpraInitatorFactory> initiaterMap;

	public XpraConnectionManager() {

		connectionFactoryMap = new HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory>();
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
		this.keyboard = new JavaFxKeyboard(new XpraKeyMap());
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

	public XpraClient createClient(IConnectionParameters params) {
		// logger.debug("creating client with params=" + params);
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			try {
				IXpraInitiator init = initiatorFactory.getXpraInitiator(params);
				Set<Integer> servers = init.getXpraServers();
				// logger.debug("displays: " + servers);
				if (!servers.contains(factory.getDisplay())) {
					// logger.debug("starting Xpra Display on " + factory.getDisplay());
					init.startXpraServer(factory.getDisplay());
					// logger.debug("Display " + d + " started");
				}
			} catch (IOException e) {
				// TODO do somehting smart
				// logger.error("Error trying to initiate Xpra", e);
			}
		}

		// TODO the fact that i need to create a new JavaFxApplicationManager, but then
		// never use it is probably an indication of an architectural problem. Think
		// further about this.
		@SuppressWarnings("unused")
		JavaFxApplicationManager applicationManager = new JavaFxApplicationManager(client, null, keyboard);
		client.connect(factory, params);
		// logger.debug("Client connected with params" + params);
		activeClientsMap.put(params, client);

		return client;
	}

	public void startApplication(IConnectionParameters params, String startCommand) {
		// logger.debug("starting application with command=" + startCommand + " params="
		// + params);
		IXpraInitatorFactory initiatorFactory = initiaterMap.get(params.getClass());
		if (initiatorFactory != null) {
			try {
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
					// TODO error?
				}

			} catch (IOException e) {
				// TODO do somehting smart
				logger.error("Error trying to initiate Xpra", e);
			}
		}

	}

}
