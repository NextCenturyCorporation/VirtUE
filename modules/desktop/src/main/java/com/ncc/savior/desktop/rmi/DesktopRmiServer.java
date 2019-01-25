package com.ncc.savior.desktop.rmi;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.AuthorizationService.ILoginListener;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.desktop.sidebar.Sidebar;
import com.ncc.savior.desktop.virtues.UserLoggedOutException;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DesktopRmiServer extends UnicastRemoteObject implements DesktopRmiInterface {
	private static final String SERVICE_NAME = "SaviorDesktop";
	public static final String RMI_NAME = "//localhost/" + SERVICE_NAME;
	private static final Logger logger = LoggerFactory.getLogger(DesktopRmiServer.class);
	private static final long serialVersionUID = 1L;
	// registry needed to prevent it from being garbage collected.
	@SuppressWarnings("unused")
	private static Registry registry;
	private VirtueService virtueService;
	private AuthorizationService authService;
	private Sidebar sidebar;

	private List<Pair<String, String>> pendingApplications;

	protected DesktopRmiServer(VirtueService virtueService, Sidebar sidebar, AuthorizationService authService)
			throws RemoteException {
		super();
		this.virtueService = virtueService;
		this.authService = authService;
		this.pendingApplications = new ArrayList<Pair<String, String>>();

		authService.addLoginListener(new ILoginListener() {

			@Override
			public void onLogin(DesktopUser user) {
				startPendingApplications();
			}

			@Override
			public void onLogout() {
				// do nothing
			}

		});
	}

	public static void bindServer(VirtueService virtueService, Sidebar sidebar, AuthorizationService authService) {
		try {
			registry = LocateRegistry.createRegistry(1099);
			Naming.rebind(RMI_NAME, new DesktopRmiServer(virtueService, sidebar, authService));
			logger.info("Desktop RMI Server ready");
		} catch (Exception e) {
			logger.error("RMI Server exception ", e);
		}
		// Runnable r = () -> {
		// try {
		// registry = LocateRegistry.createRegistry(1099);
		// Naming.rebind(RMI_NAME, new DesktopRmiServer(virtueService, sidebar));
		// logger.info("Desktop RMI Server ready");
		// } catch (Exception e) {
		// logger.error("RMI Server exception ", e);
		// }
		// };
		// // Thread t = new Thread(r, "RMI-Server");
		// // t.setDaemon(true);
		// // t.start();
		// r.run();
	}

	@Override
	public void open(String virtueId, String applicationId) throws RemoteException {
		if (isLoggedIn()) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					startApplication(virtueId, applicationId);
				}

			}, "remote application start thread");
			thread.start();
		} else {
			addToPendingApplications(virtueId, applicationId);
		}
	}

	public void startPendingApplications() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				for (Pair<String, String> pair : pendingApplications) {
					String virtueId = pair.getLeft();
					String applicationId = pair.getRight();

					// logger.info("Starting application: " + virtueId);
					startApplication(virtueId, applicationId);
				}
				pendingApplications.clear();
			}

		}, "remote application start thread");
		thread.start();
	}

	public void addToPendingApplications(String virtueId, String applicationId) {
		pendingApplications.add(Pair.of(virtueId, applicationId));
	}

	public void startApplication(String virtueId, String applicationId) {
		List<DesktopVirtue> virtues = null;
		try {
			virtues = virtueService.getVirtuesForUser();
		} catch (IOException e1) {
			logger.info("IOException");
		} catch (UserLoggedOutException e) {
			sidebar.logout(true);
			return;
		}
		for (DesktopVirtue currVirtue : virtues) {
			if (currVirtue.getTemplateId().equals(virtueId)) {
				ApplicationDefinition appDefn = currVirtue.getApps().get(applicationId);
				try {
					virtueService.startApplication(currVirtue, appDefn);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isLoggedIn() {
		boolean loggedIn = true;
		try {
			authService.getUser();
		} catch (InvalidUserLoginException e) {
			loggedIn = false;
		}

		return loggedIn;
	}

	public static void main(String[] args) {
		bindServer(null, null, null);
	}

}
