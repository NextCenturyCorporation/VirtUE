package com.ncc.savior.desktop.rmi;

import java.awt.HeadlessException;
import java.lang.management.ManagementFactory;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.sidebar.SidebarApplication;

public class RemoteStartApplication {
	private static final Logger logger = LoggerFactory.getLogger(RemoteStartApplication.class);
	private static DesktopRmiInterface lookUp;

	public static void main(String[] args) throws HeadlessException, Exception {
		List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		System.out.println("input arguments = " + inputArguments);
		System.out.println("input arguments = " + ManagementFactory.getRuntimeMXBean().getClassPath());
		remoteBrowserIpc(args);
	}

	private static void remoteBrowserIpc(String[] args) throws HeadlessException, Exception {
		String[] arguments = { "79ddefae-1573-4b47-b636-6b9fc710cfd5", "3175f4b7-5d45-45f6-b6c0-04af5a4f9520" };

		try {
			lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
			lookUp.open(arguments[0], arguments[1]);
		} catch (ConnectException e) {
			try {
				SidebarApplication.main(null);
				lookUp = (DesktopRmiInterface) Naming.lookup(DesktopRmiServer.RMI_NAME);
				lookUp.open(arguments[0], arguments[1]);
			} catch (ConnectException e1) {
				// logger.info("WHWHUHUUUU");
			}
		}

	}
}
