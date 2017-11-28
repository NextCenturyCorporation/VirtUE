package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(JschUtils.class);

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
		 com.jcraft.jsch.Logger sshLogger = new  com.jcraft.jsch.Logger() {

			@Override
			public void log(int level, String message) {
				logger.debug("level:" + level + " : " + message);
			}

			@Override
			public boolean isEnabled(int level) {
				return false;
			}
		};
		JSch.setLogger(sshLogger);
		File pem = params.getPem();
		if (pem != null && pem.exists()) {
			jsch.addIdentity(pem.getAbsolutePath());
		}
		Session session = jsch.getSession(params.getUser(), params.getHost(), params.getPort());
		session.setServerAliveInterval(1000);
		session.setServerAliveCountMax(15);
		session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setPassword(params.getPassword());
		session.setConfig("StrictHostKeyChecking", "no");
		return session;
	}

}
