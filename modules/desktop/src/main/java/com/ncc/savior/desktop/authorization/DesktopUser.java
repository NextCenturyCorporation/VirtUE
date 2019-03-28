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
package com.ncc.savior.desktop.authorization;

/**
 * Model object for the user on a desktop used for the desktop app.
 *
 *
 */
public class DesktopUser {
	private static final String DOMAIN_SEPARATOR = "\\";
	private String username;
	private String domain;

	public DesktopUser(String domain, String username) {
		this.domain = domain;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getDomain() {
		return domain;
	}

	public String getFullQualifiedDomainName() {
		return domain + DOMAIN_SEPARATOR + username;
	}

	public static DesktopUser fromFullyQualifiedDomainName(String fqd) {
		int index = fqd.lastIndexOf(DOMAIN_SEPARATOR);
		if (index < 0) {
			return new DesktopUser(null, fqd);
		}else {
			String domain = fqd.substring(0, index);
			String user = fqd.substring(index+1);
			return new DesktopUser(domain, user);
		}
	}


}
