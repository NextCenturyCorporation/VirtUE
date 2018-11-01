package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.ncc.savior.virtueadmin.model.NetworkProtocol;

/**
 * Application Data Transfer Object (DTO).
 *
 *
 */
@Entity
public class WhitelistedNetwork {
	@Id
	private String id;
	private String host;
	private NetworkProtocol protocol;
	private int localPort;
	private int remotePort;

	public WhitelistedNetwork(String id, String host, NetworkProtocol protocol, int localPort, int remotePort) {
		super();
		this.id = id;
		this.host = host;
		this.protocol = protocol;
		this.localPort = localPort;
		this.remotePort = remotePort;
	}

	/**
	 * Used for jackson deserialization
	 * This file was copied from ApplicationDefinition - don't know if we still need this.
	 */
	protected WhitelistedNetwork() {

	}

	public WhitelistedNetwork(String id, WhitelistedNetwork wlNetwork) {
		this.id = id;
		this.host = wlNetwork.getHost();
		this.protocol = wlNetwork.getProtocol();
		this.localPort = wlNetwork.getLocalPort();
		this.remotePort = wlNetwork.getRemotePort();
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public NetworkProtocol getProtocol() {
		return protocol;
	}

	public int getLocalPort() {
		return localPort;
	}

	public int getRemotePort() {
		return remotePort;
	}

	// below setters used for jackson deserialization
	public void setId(String id) {
		this.id = id;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setProtocol(NetworkProtocol protocol) {
		this.protocol = protocol;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	@Override
	public String toString() {
		return "Whitelisted Network: [id=" + id + ", host=" + host + ", protocol=" + protocol + ", localPort=" + localPort
				+ ", remotePort=" + remotePort + "]";
	}

	/**
	 * Is an int actually big enough? #TODO
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
		// result = prime * result + ((localPort == -1) ? 0 : localPort);
		// result = prime * result + ((remotePort == -1) ? 0 : remotePort);
		result = prime * result + localPort;
		result = prime * result + remotePort;
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
		WhitelistedNetwork other = (WhitelistedNetwork) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (protocol == null) {
			if (other.protocol != null)
				return false;
		} else if (!protocol.equals(other.protocol))
			return false;
		if (localPort != other.localPort)
			return false;
		if (remotePort != other.remotePort)
			return false;
		return true;
	}
	public static final Comparator<? super WhitelistedNetwork> CASE_INSENSITIVE_NAME_COMPARATOR = new CaseInsensitiveNameComparator();
	private static class CaseInsensitiveNameComparator implements Comparator<WhitelistedNetwork> {
		@Override
		public int compare(WhitelistedNetwork o1, WhitelistedNetwork o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getHost(), o2.getHost());
		}
	}
}
