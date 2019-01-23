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
		String[] arguments = { "79ddefae-1573-4b47-b636-6b9fc710cfd5", "3175f4b7-5d45-45f6-b6c0-04af5a4f9520" };
		String virtueId = arguments[0];
		String applicationId = arguments[1];

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
