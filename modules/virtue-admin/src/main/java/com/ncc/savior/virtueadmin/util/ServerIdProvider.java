package com.ncc.savior.virtueadmin.util;

import java.util.Map;

import com.ncc.savior.util.JavaUtil;

/**
 * Provides a server ID such that multiple servers can operate on the same AWS
 * account and still be identifiable and segmented. This is particularly useful
 * in development when there may be multiple developers as well as multiple
 * integration environments (I.E. Development, Integration, Production, Test).
 * Server Id is determined based on the following precedence:
 * 
 * <ol>
 * <li>Java property "virtue.serverId"
 * <li>Property passed to the {@link ServerIdProvider}. Most likely from
 * "virtue.serverId" in savior-server.properties.
 * <li>Default based on local hostname and username.
 * </ol>
 *
 */
public class ServerIdProvider {

	private static final String PROPERTY_SERVER_ID = "virtue.serverId";
	private String serverId;

	public ServerIdProvider(String value) {
		this.serverId = createDefaultServerId();
		if (JavaUtil.isNotEmpty(value)) {
			this.serverId = value;
		}
		String prop = System.getProperty(PROPERTY_SERVER_ID);
		if (JavaUtil.isNotEmpty(prop)) {
			this.serverId = prop;
		}
		serverId = serverId.replaceAll("-", "_");
	}

	private String createDefaultServerId() {
		String serverId;
		String username = System.getProperty("user.name");

		String computerName;
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			computerName = env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			computerName = env.get("HOSTNAME");
		else
			computerName = null;

		if (username == null) {
			if (computerName == null) {
				serverId = "default";
			} else {
				serverId = computerName;
			}
		} else {
			serverId = username;
			if (computerName != null) {
				serverId = computerName + "_" + username;
			}
		}
		return serverId;
	}

	public String getServerId() {
		return serverId;
	}
}
