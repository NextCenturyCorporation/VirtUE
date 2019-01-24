package com.ncc.savior.virtueadmin.model;

import java.util.Comparator;

import javax.persistence.Embeddable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Application Data Transfer Object (DTO).
 *
 * If get/set-Protocol are still commented out, then this still is broken - saving enums to the h2db like this doesn't work.
 * When that is fixed, remember to remove the hard-coding to this.protocol in the default constructor.
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
		// logger.debug("In first WhitelistedNetwork constructor");
		this.host = host;
		this.localPort = localPort;
		this.remotePort = remotePort;
		this.protocol = protocol;
	}

	/**
	 * Used for jackson deserialization
	 */
	protected WhitelistedNetwork() {
		// logger.debug("In second WhitelistedNetwork constructor");
		this.protocol = NetworkProtocol.TCPIP;
	}

	public WhitelistedNetwork(WhitelistedNetwork wlNetwork) {
		// logger.debug("In third WhitelistedNetwork constructor");
		this.host = wlNetwork.getHost();
		this.localPort = wlNetwork.getLocalPort();
		this.remotePort = wlNetwork.getRemotePort();
		// this.protocol = wlNetwork.getProtocol();
	}

	@JsonGetter
	public String getHost() {
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
		// logger.debug("setHost " + host);
		this.host = host;
	}

	@JsonSetter
	public void setLocalPort(int localPort) {
		// logger.debug("setLocalPort " + localPort);
		this.localPort = localPort;
	}

	@JsonSetter
	public void setRemotePort(int remotePort) {
		// logger.debug("setRemotePort " + remotePort);
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
