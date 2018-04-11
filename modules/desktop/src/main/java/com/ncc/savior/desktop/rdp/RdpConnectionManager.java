package com.ncc.savior.desktop.rdp;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.SocketUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.network.JschUtils;
import com.ncc.savior.network.SshConnectionParameters;
import com.ncc.savior.virtueadmin.model.desktop.BaseApplicationInstance;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class RdpConnectionManager {
	private static final int RDP_PORT = 3389;

	Map<String, Session> sessions;

	public RdpConnectionManager() {
		sessions = new HashMap<>();
	}

	public void startApplication(DesktopVirtue virtue, BaseApplicationInstance appInstance, RgbColor color,
			SshConnectionParameters connectionParameters) throws JSchException {
		Session session = getSession(connectionParameters);
		int randomPort = SocketUtils.findAvailableTcpPort();
		session.setPortForwardingR(RDP_PORT, "localhost", randomPort);
		// configure rdp connection parameters
		// start rdp connection
		// start thread to manage connection?
	}

	private Session getSession(SshConnectionParameters connectionParams) throws JSchException {
		Session session = sessions.get(connectionParams.getHost());
		if (session == null) {
			session = JschUtils.getSession(connectionParams);
			session.connect();
			sessions.put(connectionParams.getHost(), session);
		}
		return session;
	}

}
