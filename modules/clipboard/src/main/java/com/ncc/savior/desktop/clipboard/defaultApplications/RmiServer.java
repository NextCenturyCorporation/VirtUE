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

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.IClipboardMessageSenderReceiver;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

/**
 * Server side of RMI. This is hosted in the main application on a virtue VM.(at
 * the moment clipboard)
 * 
 *
 */
public class RmiServer extends UnicastRemoteObject implements RmiInterface {
	private static final String SERVICE_NAME = "SaviorDefaultApplication";
	public static final String RMI_NAME = "//localhost/" + SERVICE_NAME;
	private static final Logger logger = LoggerFactory.getLogger(RmiServer.class);
	private static final long serialVersionUID = 1L;
	// registry needed to prevent it from being garbage collected.
	@SuppressWarnings("unused")
	private static Registry registry;
	private IClipboardMessageSenderReceiver transmitter;
	private String sourceId;

	protected RmiServer(String sourceId, IClipboardMessageSenderReceiver transmitter) throws RemoteException {
		super();
		this.transmitter = transmitter;
		this.sourceId = sourceId;
	}

	public static void bindServer(String sourceId, IClipboardMessageSenderReceiver transmitter) {
		Runnable r = () -> {
			try {
				registry = LocateRegistry.createRegistry(1099);
				Naming.rebind(RMI_NAME, new RmiServer(sourceId, transmitter));
				logger.info("RMI Server ready");
			} catch (Exception e) {
				logger.error("RMI Server exception ", e);
			}
		};
		// Thread t = new Thread(r, "RMI-Server");
		// t.setDaemon(true);
		// t.start();
		r.run();
	}

	@Override
	public void open(DefaultApplicationType app, String[] args) throws RemoteException {
		DefaultApplicationMessage msg = new DefaultApplicationMessage(sourceId, app, args);
		try {
			transmitter.sendMessageToHub(msg);
		} catch (IOException e) {
			logger.error("Error sending DefaultApplicationMessage " + msg);
		}
	}

	public static void main(String[] args) {
		bindServer(null, null);
	}

}