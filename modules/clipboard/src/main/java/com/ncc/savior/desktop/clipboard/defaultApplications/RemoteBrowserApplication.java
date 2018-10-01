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
	private static RmiInterface look_up;

	public static void main(String[] args) {
		remoteBrowserIpc(args);
	}

	private static void remoteBrowserIpc(String[] args) {
		try {
			logger.debug("looking up rmi");
			look_up = (RmiInterface) Naming.lookup(RmiServer.RMI_NAME);
			look_up.open(DefaultApplicationType.BROWSER, args);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			logger.error("Error sending Rmi", e);
		}

	}
}
