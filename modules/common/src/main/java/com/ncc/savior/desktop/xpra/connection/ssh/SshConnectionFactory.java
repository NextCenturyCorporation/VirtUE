package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.File;
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

/**
 * Factory class to create a {@link IConnection} using SSH.
 *
 *
 */
public class SshConnectionFactory extends BaseConnectionFactory {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SshConnectionFactory.class);
	// private static final String DEFAULT_COMMAND_DIR = "/run/user/1000/xpra/";
	// private static final String DEFAULT_COMMAND_NAME = "run-xpra";

	private static final String DEFAULT_COMMAND_DIR = "";
	private static final String DEFAULT_COMMAND_NAME = "xpra";

	private static final String DEFAULT_COMMAND_MODE = "_proxy";
	private static final int DEFAULT_DISPLAY = 55;
	private String commandDir;
	private String commandName;
	private String commandMode;
	private int display;

	public SshConnectionFactory() {
		this(DEFAULT_COMMAND_DIR, DEFAULT_COMMAND_NAME, DEFAULT_COMMAND_MODE, DEFAULT_DISPLAY);
	}

	public SshConnectionFactory(String commandDir, String commandName, String commandMode, int display) {
		this.commandDir = commandDir;
		this.commandName = commandName;
		this.commandMode = commandMode;
		this.display = display;
	}

	@Override
	protected IConnection doConnect(IConnectionParameters params) throws IOException {
		if (params instanceof SshConnectionParameters) {
			try {
				SshConnectionParameters p = (SshConnectionParameters) params;
				Session session = JschUtils.getSession(p);
				session.connect();
				ChannelExec channel = (ChannelExec) session.openChannel("exec");
				String command = getCommand(commandDir, commandName, commandMode, display);
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

	public static class SshConnectionParameters implements IConnectionParameters {
		private final int port;
		private final String host;
		private final String user;
		private final String password;
		private final File pem;

		public SshConnectionParameters(String host, int port, String user, String password) {
			this.host = host;
			this.port = port;
			this.user = user;
			this.password = password;
			this.pem = null;
		}

		public SshConnectionParameters(String host, int port, String user, File pem) {
			this.host = host;
			this.port = port;
			this.pem = pem;
			this.user = user;
			this.password = null;
		}

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}

		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public File getPem() {
			return pem;
		}

		@Override
		public String toString() {
			return "SshConnectionParameters [port=" + port + ", host=" + host + ", user=" + user + ", password="
					+ password + ", pem=" + pem + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + ((password == null) ? 0 : password.hashCode());
			result = prime * result + ((pem == null) ? 0 : pem.hashCode());
			result = prime * result + port;
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SshConnectionParameters other = (SshConnectionParameters) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (password == null) {
				if (other.password != null)
					return false;
			} else if (!password.equals(other.password))
				return false;
			if (pem == null) {
				if (other.pem != null)
					return false;
			} else if (!pem.equals(other.pem))
				return false;
			if (port != other.port)
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}

		@Override
		public String getConnectionKey() {
			return host + "-" + port + "-" + user;
		}
	}

	@Override
	public int getDisplay() {
		return display;
	}
}
