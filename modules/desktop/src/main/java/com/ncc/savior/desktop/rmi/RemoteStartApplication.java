package com.ncc.savior.desktop.rmi;

import java.awt.HeadlessException;
import java.rmi.ConnectException;
import java.rmi.Naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.client.ClipboardClient;
import com.ncc.savior.desktop.sidebar.SidebarApplication;

public class RemoteStartApplication {
	private static final Logger logger = LoggerFactory.getLogger(ClipboardClient.class);
	private static DesktopRmiInterface lookUp;

	public static void main(String[] args) throws HeadlessException, Exception {
		remoteBrowserIpc(args);
	}

	private static void remoteBrowserIpc(String[] args) throws HeadlessException, Exception {
		String[] arguments = { "225a171c-f345-426a-812c-e55bae46253b", "1d7c423c-8708-4b49-8f6d-808082932e0e" };

		try {
			lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
			lookUp.open(arguments);
		} catch (ConnectException e) {
			try {
				SidebarApplication.main(null);
				lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
				lookUp.open(arguments);
			} catch (ConnectException e1) {
				logger.info("WHWHUHUUUU");
			}
		}

	}
}
