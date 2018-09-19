package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

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
