/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.connection.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.ncc.savior.desktop.xpra.connection.BaseConnection;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;

/**
 * Xpra connection implemented via SSH
 *
 *
 */
public class SshConnection extends BaseConnection {
	private static final Logger logger = LoggerFactory.getLogger(SshConnection.class);

	// private Session session;
	private ChannelExec channel;
	private InputStream in;
	private OutputStream out;

	private boolean logErrorStream = false;

	public SshConnection(SshConnectionParameters p, Session session, ChannelExec channel) throws IOException {
		super(p);
		// this.session = session;
		this.channel = channel;
		if (logErrorStream) {
			BufferedReader err = new BufferedReader(new InputStreamReader(channel.getErrStream()));
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					String line;
					try {
						while ((line = err.readLine()) != null) {
							logger.debug(line);
						}
					} catch (IOException e) {
						logger.warn("Error attempting to read SshConnection error stream", e);
					}

				}
			});
			t.start();
		}
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
