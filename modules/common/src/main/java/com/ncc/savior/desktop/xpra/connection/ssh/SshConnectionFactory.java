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
				Session session = JschUtils.getUnconnectedSession(p);
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

	public static class SshConnectionParameters implements IConnectionParameters {
		private final int port;
		private final String host;
		private final String user;
		private final String password;
		private final String pemString;
		private final File pemFile;
		private int display;

		protected SshConnectionParameters(String host, int port, String user, String password, String pem,
				File pemFile) {
			this.host = host;
			this.port = port;
			this.user = user;
			this.password = password;
			this.pemString = pem;
			this.pemFile = pemFile;
		}

		// statics created only because its much easier documenation
		public static SshConnectionParameters withPassword(String host, int port, String user, String password) {
			return new SshConnectionParameters(host, port, user, password, null, null);
		}

		public static SshConnectionParameters withPemString(String host, int port, String user, String pem) {
			return new SshConnectionParameters(host, port, user, null, pem, null);
		}

		public static SshConnectionParameters withExistingPemFile(String host, int port, String user, File pem) {
			return new SshConnectionParameters(host, port, user, null, null, pem);
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

		public String getPemString() {
			return pemString;
		}

		public File getPemFile() {
			return pemFile;
		}

		@Override
		public String toString() {
			return "SshConnectionParameters [port=" + port + ", host=" + host + ", user=" + user + ", password="
					+ (password == null ? null : "[protected]") + ", pemString="
					+ (pemString == null ? null : "[protected]") + ", pemFile=" + pemFile + ", display=" + display
					+ "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + ((password == null) ? 0 : password.hashCode());
			result = prime * result + ((pemFile == null) ? 0 : pemFile.hashCode());
			result = prime * result + ((pemString == null) ? 0 : pemString.hashCode());
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
			if (pemFile == null) {
				if (other.pemFile != null)
					return false;
			} else if (!pemFile.equals(other.pemFile))
				return false;
			if (pemString == null) {
				if (other.pemString != null)
					return false;
			} else if (!pemString.equals(other.pemString))
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

		@Override
		public int getDisplay() {
			return display;
		}

		@Override
		public void setDisplay(int display) {
			this.display = display;
		}
	}

	// @Override
	// public int getDisplay() {
	// return display;
	// }
}
