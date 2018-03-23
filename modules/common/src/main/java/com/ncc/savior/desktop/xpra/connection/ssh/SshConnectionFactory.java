package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.network.JschUtils;
import com.ncc.savior.network.SshConnectionParameters;

/**
 * Factory class to create a {@link IConnection} using SSH.
 *
 *
 */
public class SshConnectionFactory extends BaseConnectionFactory {
	private static final Logger logger = LoggerFactory.getLogger(SshConnectionFactory.class);
	// private static final String DEFAULT_COMMAND_DIR = "/run/user/1000/xpra/";
	// private static final String DEFAULT_COMMAND_NAME = "run-xpra";

	private static final String DEFAULT_COMMAND_DIR = "";
	private static final String DEFAULT_COMMAND_NAME = "xpra";

	private static final String DEFAULT_COMMAND_MODE = "_proxy";
	// private static final int DEFAULT_DISPLAY = 55;
	private String commandDir;
	private String commandName;
	private String commandMode;

	public SshConnectionFactory() {
		this(DEFAULT_COMMAND_DIR, DEFAULT_COMMAND_NAME, DEFAULT_COMMAND_MODE);
	}

	public SshConnectionFactory(String commandDir, String commandName, String commandMode) {
		this.commandDir = commandDir;
		this.commandName = commandName;
		this.commandMode = commandMode;
	}

	@Override
	protected IConnection doConnect(IConnectionParameters params) throws IOException {
		if (params instanceof SshConnectionParameters) {
			try {
				SshConnectionParameters p = (SshConnectionParameters) params;
				Session session = JschUtils.getSession(p);
				session.connect();
				ChannelExec channel = (ChannelExec) session.openChannel("exec");
				String command = getCommand(commandDir, commandName, commandMode, p.getDisplay());
				logger.debug("connecting with command=" + command);
				channel.setCommand(command);
				channel.connect();
				return new SshConnection(p, session, channel);
			} catch (JSchException e) {
				throw new IOException(e);
			}
		} else {
			throw new InvalidParameterException(
					"Connection factory and connection parameter combination cannot create connection.  FactoryClass="
							+ this.getClass().getCanonicalName() + " ParameterClass="
							+ params.getClass().getCanonicalName());
		}
	}

	private String getCommand(String dir, String name, String mode, int display) {
		return dir + name + " " + mode + " :" + display;
	}

	// @Override
	// public int getDisplay() {
	// return display;
	// }
}
