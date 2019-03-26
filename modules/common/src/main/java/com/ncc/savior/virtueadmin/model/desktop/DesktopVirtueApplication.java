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
package com.ncc.savior.virtueadmin.model.desktop;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;

public class DesktopVirtueApplication {
	private String id;
	private String name;
	private String version;
	private OS os;
	private String hostname;
	private int port;
	private String userName;
	private String privateKey;
	private String windowsApplicationPath;

	public DesktopVirtueApplication(String id, String name, String version, OS os, String hostname, int port,
			String userName, String privateKey, String windowsApplicationPath) {
		super();
		this.id = id;
		this.name = name;
		this.version = version;
		this.os = os;
		this.hostname = hostname;
		this.port = port;
		this.userName = userName;
		this.privateKey = privateKey;
		this.windowsApplicationPath = windowsApplicationPath;
	}

	protected DesktopVirtueApplication() {

	}

	public DesktopVirtueApplication(ApplicationDefinition application, String hostname, int sshPort, String userName,
			String privateKey) {
		this(application.getId(), application.getName(), application.getVersion(), application.getOs(), hostname,
				sshPort, userName, privateKey, application.getLaunchCommand());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public OS getOs() {
		return os;
	}

	public void setOs(OS os) {
		this.os = os;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getWindowsApplicationPath() {
		return windowsApplicationPath;
	}

	protected void setWindowsApplicationPath(String windowsApplicationPath) {
		this.windowsApplicationPath = windowsApplicationPath;
	}

	@Override
	public String toString() {
		return "DesktopVirtueApplication [id=" + id + ", name=" + name + ", version=" + version + ", os=" + os
				+ ", hostname=" + hostname + ", port=" + port + ", userName=" + userName + ", privateKey="
				+ "[protected]" + ", windowsApplicationPath=" + windowsApplicationPath + "]";
	}
}
