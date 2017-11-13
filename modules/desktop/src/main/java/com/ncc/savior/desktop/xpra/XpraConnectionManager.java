package com.ncc.savior.desktop.xpra;

import java.util.HashMap;

import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManager;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory.TcpConnectionParameters;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;

//TODO review JavaFX specific code here.
public class XpraConnectionManager {
	private HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory> connectionFactoryMap;
	private HashMap<IConnectionParameters, XpraClient> activeClientsMap;
	private JavaFxKeyboard keyboard;
	// initiaterMap

	public XpraConnectionManager() {
		connectionFactoryMap = new HashMap<Class<? extends IConnectionParameters>, BaseConnectionFactory>();
		connectionFactoryMap.put(TcpConnectionParameters.class, new TcpConnectionFactory());
		// TODO add initiators
		// initiaterMap = new HashMap<Class<? extends IConnectionParameters>,
		// BaseConnectionFactory>();
		// initiaterMap.put(SshConnectionParameters.class, new SshXpraInitiater());
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
		BaseConnectionFactory factory = connectionFactoryMap.get(params.getClass());
		XpraClient client = new XpraClient();
		// TODO the fact that i need to create a new JavaFxApplicationManager, but then
		// never use it is probably an indication of an architectural problem. Think
		// further about this.
		@SuppressWarnings("unused")
		JavaFxApplicationManager applicationManager = new JavaFxApplicationManager(client, null, keyboard);
		client.connect(factory, params);
		activeClientsMap.put(params, client);

		return client;
	}

	public void startApplication(IConnectionParameters params) {
		// TODO Auto-generated method stub

	}

}
