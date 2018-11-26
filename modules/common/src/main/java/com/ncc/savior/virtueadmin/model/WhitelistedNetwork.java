package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.ncc.savior.virtueadmin.model.NetworkProtocol;

/**
 * Application Data Transfer Object (DTO).
 *
 *
 */
@Embeddable
public class WhitelistedNetwork {
	@JsonIgnore
	private static final Logger logger = LoggerFactory.getLogger(WhitelistedNetwork.class);

	private String host;
	private int localPort;
	private int remotePort;

	// @Embedded
	// @Enumerated(EnumType.STRING)
	private NetworkProtocol protocol;

	public WhitelistedNetwork(String host, NetworkProtocol protocol, int localPort, int remotePort) {
		logger.debug("here2 ");
		this.host = host;
		this.localPort = localPort;
		this.remotePort = remotePort;
		this.protocol = protocol;
	}

	/**
	 * Used for jackson deserialization
	 * This file was copied from ApplicationDefinition - don't know if we still need this.
	 */
	protected WhitelistedNetwork() {

		logger.debug("here ");
		protocol = NetworkProtocol.TCPIP;
	}

	public WhitelistedNetwork(WhitelistedNetwork wlNetwork) {
		logger.debug("here3 ");
		this.host = wlNetwork.getHost();
		this.localPort = wlNetwork.getLocalPort();
		this.remotePort = wlNetwork.getRemotePort();
		// this.protocol = wlNetwork.getProtocol();
	}

	@JsonGetter
	public String getHost() {
		logger.debug("getHost");
		return host;
	}

	@JsonGetter
	public int getLocalPort() {
		return localPort;
	}

	@JsonGetter
	public int getRemotePort() {
		return remotePort;
	}

	// @JsonGetter
	// public NetworkProtocol getProtocol() {
	// 	return protocol;
	// }

	// below setters used for jackson deserialization
	@JsonSetter
	public void setHost(String host) {
		logger.debug("setHost " + host);
		this.host = host;
	}

	@JsonSetter
	public void setLocalPort(int localPort) {
		logger.debug("setLocalPort " + localPort);
		this.localPort = localPort;
	}

	@JsonSetter
	public void setRemotePort(int remotePort) {
		logger.debug("setRemotePort " + remotePort);
		this.remotePort = remotePort;
	}

	// @JsonSetter
	// public void setProtocol(NetworkProtocol protocol) {
	// 	logger.debug("setProtocol " + protocol);
	// 	this.protocol = protocol;
	// }

	@Override
	public String toString() {
		return "Whitelisted Network: [host=" + host + ", localPort=" + localPort
				+ ", remotePort=" + remotePort + ", protocol=" + protocol + "]";
	}

	/**
	 * Is an int actually big enough? #TODO
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

		// check if both null or same reference, and if not, then check equals.
		if (host != other.host || !host.equals(other.getHost())) {
			return false;
		}
		// if (protocol != other.protocol || !protocol.equals(other.getProtocol())) {
		// 	return false;
		// }
		if (localPort != other.getLocalPort()) {
			return false;
		}
		if (remotePort != other.getRemotePort()) {
			return false;
		}
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
