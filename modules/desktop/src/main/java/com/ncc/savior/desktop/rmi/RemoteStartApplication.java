package com.ncc.savior.desktop.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.clipboard.defaultApplications.RmiInterface;
import com.ncc.savior.desktop.clipboard.defaultApplications.RmiServer;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public class RemoteStartApplication {
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
