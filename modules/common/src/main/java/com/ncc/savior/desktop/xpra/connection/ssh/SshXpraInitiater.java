package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.IXpraInitiator;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

/**
 * Helper class that starts applications and controls Xpra over SSH.
 *
 *
 */
public class SshXpraInitiater implements IXpraInitiator {
	private static final Logger logger = LoggerFactory.getLogger(SshXpraInitiater.class);
	private static final String SUDO_OR_NOTHING = "";
	private static final String XPRA_CMD = SUDO_OR_NOTHING + " xpra";
	private static final String XPRA_STOP = XPRA_CMD + " stop";
	private static final String XPRA_START = XPRA_CMD + " start";
	private static final String XPRA_LIST = XPRA_CMD + " list";

	private SshConnectionParameters params;

	public SshXpraInitiater(SshConnectionParameters params) {
		this.params = params;
	}

	@Override
	public Set<Integer> getXpraServers() throws IOException {
		Session session = null;
		ChannelExec channel = null;
		Set<Integer> set = new TreeSet<Integer>();
		try {
			session = getConnectedSession();
			channel = getConnectedChannel(XPRA_LIST, session, null);
			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			String line;
			while ((line = reader.readLine()) != null) {
				String prefix = "LIVE session at :";
				if (line.contains(prefix)) {
					String displayStr = line.substring(line.indexOf(prefix) + prefix.length());
					int display = Integer.parseInt(displayStr);
					set.add(display);
				}
				// System.out.println(line);
			}
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return set;
	}

	@Override
	public int startXpraServer(int display) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		try {
			session = getConnectedSession();
			session.setTimeout(10000);
			String command = (display > 0 ? XPRA_START + " :" + display : XPRA_START);
			// command = "sudo systemctl enable xpra.socket;" + command;
			channel = getConnectedChannel(command, session, null);
			channel.setErrStream(System.err);
			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			channel.connect();
			String line;
			Thread.sleep(300);
			while ((line = reader.readLine()) != null) {
				String prefix = "available on display :";
				if (line.contains(prefix)) {
					String displayStr = line.substring(line.indexOf(prefix) + prefix.length());
					display = Integer.parseInt(displayStr);
				}
				// logger.debug(line);
			}
		} catch (JSchException | InterruptedException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return display;
	}

	@Override
	public boolean stopXpraServer(int display) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		boolean success = false;
		try {
			session = getConnectedSession();
			channel = getConnectedChannel(XPRA_STOP + " :" + display, session, null);
			InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			BufferedReader reader = new BufferedReader(stream);
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(":" + display + "has exited")) {
					return true;
				}
			}
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
		return success;
	}

	@Override
	public void startXpraApp(int display, String command) throws IOException {
		Session session = null;
		ChannelExec channel = null;
		try {
			session = getConnectedSession();
			String fullCommand = "export DISPLAY=:" + display + ";" + SUDO_OR_NOTHING + command;
			channel = getConnectedChannel(fullCommand, session, null);
			// InputStreamReader stream = new InputStreamReader(channel.getInputStream());
			// BufferedReader reader = new BufferedReader(stream);
			// String line;
			// while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			// }
		} catch (JSchException e) {
			throw new IOException(e);
		} finally {
			closeAll(session, channel);
		}
	}

	@Override
	public void stopAllXpraServers() throws IOException {
		Set<Integer> displays = getXpraServers();
		for (Integer display : displays) {
			stopXpraServer(display);
		}
	}

	private ChannelExec getConnectedChannel(String command, Session session, OutputStream out) throws JSchException {
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		channel.setErrStream(System.err);

		channel.connect();
		return channel;
	}

	private Session getConnectedSession() throws JSchException {
		Session session = JschUtils.getSession(params);
		session.connect();
		return session;
	}

	private void closeAll(Session session, ChannelExec channel) {
		if (channel != null) {
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}

	public static final void main(String[] args) throws IOException {
		SshConnectionParameters p = new SshConnectionParameters("localhost", 22, "user", "password");
		SshXpraInitiater init = new SshXpraInitiater(p);

		Set<Integer> displays = init.getXpraServers();
		System.out.println(displays);
		//
		init.stopAllXpraServers();
		//
		// displays = init.getXpraServers();
		// System.out.println(displays);

		int display = init.startXpraServer(8);
		System.out.println("started on " + display);

		displays = init.getXpraServers();
		System.out.println(displays);

		init.startXpraApp(display, "gedit");

	}

}
