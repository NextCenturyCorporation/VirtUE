package com.ncc.savior.desktop.xpra;

import java.util.HashMap;

import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManager;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory.TcpConnectionParameters;

public class XpraConnectionManager {
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<IConnectionParameters, XpraClient> activeClientsMap;
	// initiaterMap

	public XpraConnectionManager() {
		connectionFactoryMap = new HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory>();
		connectionFactoryMap.put(TcpConnectionParameters.class, new TcpConnectionFactory());
		// TODO add initiators
		// initiaterMap = new HashMap<Class<? extends IConnectionParameters>,
		// BaseConnectionFactory>();
		// initiaterMap.put(SshConnectionParameters.class, new SshXpraInitiater());
		activeClientsMap = new HashMap<IConnectionParameters, XpraClient>();
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
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		JavaFxApplicationManager applicationManager = new JavaFxApplicationManager(client, null);
		client.connect(factory, params);
		activeClientsMap.put(params, client);
		return client;
	}

	public void startApplication(IConnectionParameters params) {
		// TODO Auto-generated method stub

	}

}
