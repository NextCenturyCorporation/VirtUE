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
