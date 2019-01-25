package com.ncc.savior.desktop.rmi;

import java.awt.HeadlessException;
import java.rmi.ConnectException;
import java.rmi.Naming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.sidebar.SidebarApplication;

public class RemoteStartApplication {
	private static final Logger logger = LoggerFactory.getLogger(RemoteStartApplication.class);
	private static DesktopRmiInterface lookUp;

	public static void main(String[] args) throws HeadlessException, Exception {
		remoteApplicationIpc(args);
	}

	private static void remoteApplicationIpc(String[] args) throws HeadlessException, Exception {
		String virtueId = args[0];
		String applicationId = args[1];

		try {
			lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
			lookUp.open(virtueId, applicationId);
		} catch (ConnectException e) {
			try {
				SidebarApplication.main(null);
				lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
				lookUp.open(virtueId, applicationId);
			} catch (ConnectException e1) {
				logger.debug("error remotely starting application");
			}
		}

	}
}
