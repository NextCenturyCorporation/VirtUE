package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;

public class SshConnectionFactory extends BaseConnectionFactory {
	private static final Logger logger = LoggerFactory.getLogger(SshConnectionFactory.class);
	private static final String DEFAULT_COMMAND_DIR = "/run/user/1000/xpra/";
	private static final String DEFAULT_COMMAND_NAME = "run-xpra";
	private static final String DEFAULT_COMMAND_MODE = "_proxy";
	private static final int DEFAULT_DISPLAY = 8;
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
				JSch jsch = new JSch();
				Session session;
				session = jsch.getSession(p.getUser(), p.getHost(), p.getPort());
				session.setServerAliveInterval(1000);
				session.setServerAliveCountMax(15);
				session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
				session.setPassword(p.getPassword());
				session.setConfig("StrictHostKeyChecking", "no");
				session.connect();
				ChannelExec channel = (ChannelExec) session.openChannel("exec");
				String command = getCommand(commandDir, commandName, commandMode, display);
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

		public SshConnectionParameters(String host, int port, String user, String password) {
			this.host = host;
			this.port = port;
			this.user = user;
			this.password = password;
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

		@Override
		public String toString() {
			return "SshConnectionParameters [port=" + port + ", host=" + host + ", user=" + user + ", password="
					+ password + "]";
		}
	}
}
