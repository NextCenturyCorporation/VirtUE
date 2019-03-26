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
package com.ncc.savior.desktop.xpra.connection.tcp;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.connection.BaseConnectionFactory;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;

public class TcpConnectionFactory extends BaseConnectionFactory {
	public static final Logger logger = LoggerFactory.getLogger(TcpConnectionFactory.class);

	public TcpConnectionFactory() {

	}

	@Override
	protected IConnection doConnect(IConnectionParameters params) throws IOException {
		if (params instanceof TcpConnectionParameters) {
			TcpConnectionParameters p = (TcpConnectionParameters) params;
			Socket socket = new Socket(p.getHost(), p.getPort());
			socket.setKeepAlive(true);
			return new TcpConnection(p, socket);
		} else {
			throw new InvalidParameterException(
					"Connection factory and connection parameter combination cannot create connection.  FactoryClass="
							+ this.getClass().getCanonicalName() + " ParameterClass="
							+ params.getClass().getCanonicalName());
		}
	}

	public static class TcpConnectionParameters implements IConnectionParameters {
		private final int port;
		private final String host;
		private int display;

		public TcpConnectionParameters(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}

		@Override
		public String toString() {
			return "TcpConnectionParameters [port=" + port + ", host=" + host + "]";
		}

		@Override
		public String getConnectionKey() {
			return host + "-" + port;
		}

		@Override
		public void setDisplay(int display) {
			this.display = display;
		}

		@Override
		public int getDisplay() {
			return display;
		}
	}

	/**
	 * Not used for TCP connections.
	 */
	// @Override
	// public int getDisplay() {
	// return -1;
	// }
}
