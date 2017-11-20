package com.ncc.savior.desktop.xpra.connection.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

/**
 * Used for a single place where default connection options are set for SSH.
 *
 *
 */
public class JschUtils {

	/**
	 * Session needs to be connected by calling .connect()
	 *
	 * @param params
	 * @return
	 * @throws JSchException
	 */
	// TODO review parameters for sanity!
	public static Session getSession(SshConnectionParameters params) throws JSchException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(params.getUser(), params.getHost(), params.getPort());
		session.setServerAliveInterval(1000);
		session.setServerAliveCountMax(15);
		session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setPassword(params.getPassword());
		session.setConfig("StrictHostKeyChecking", "no");
		return session;
	}

}
