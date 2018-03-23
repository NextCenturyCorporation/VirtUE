package com.ncc.savior.network;

import java.io.File;

import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;

public class SshConnectionParameters implements IConnectionParameters {
	private final int port;
	private final String host;
	private final String user;
	private final String password;
	private final File pem;
	private int display;

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

	public int getDisplay() {
		return display;
	}

	@Override
	public void setDisplay(int display) {
		this.display = display;
	}
}