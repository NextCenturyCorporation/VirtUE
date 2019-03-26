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
package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

/**
 * Simple java application that acts as a browser from the command line. The
 * intent is this should be the default browser for a virtue VM. When the
 * browser is invoked through the OS's default application mechanism, this
 * application will use RMI to connect to the clipboard app on the VM. This app
 * will send the parameters to clipboard which will send it to the desktop for
 * handling.
 *
 */
public class RemoteBrowserApplication {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private static RmiInterface lookUp;

	public static void main(String[] args) {
		remoteBrowserIpc(args);
	}

	private static void remoteBrowserIpc(String[] args) {
		try {
			logger.debug("looking up rmi");
			lookUp = (RmiInterface) Naming.lookup(RmiServer.RMI_NAME);
			lookUp.open(DefaultApplicationType.BROWSER, args);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			logger.error("Error sending Rmi", e);
		}

	}
}
