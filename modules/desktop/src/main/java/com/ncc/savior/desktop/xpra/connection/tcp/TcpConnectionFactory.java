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
	}

	/**
	 * Not used for TCP connections.
	 */
	@Override
	public int getDisplay() {
		return -1;
	}
}
