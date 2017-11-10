package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.BaseConnection;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

public class SshConnection extends BaseConnection {
	private static final Logger logger = LoggerFactory.getLogger(SshConnection.class);

	private Session session;
	private ChannelExec channel;
	private InputStream in;
	private OutputStream out;

	public SshConnection(SshConnectionParameters p, Session session, ChannelExec channel) throws IOException {
		super(p);
		this.session = session;
		this.channel = channel;

		in = channel.getInputStream();
		out = channel.getOutputStream();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public boolean isActive() {
		return channel.isConnected() && !channel.isClosed();
	}

	public void start() throws IOException {
		try {
			channel.connect();
			logger.info("Start Xpra connection...");
		} catch (JSchException e) {
			throw new IOException(e);
		}
	}

}
