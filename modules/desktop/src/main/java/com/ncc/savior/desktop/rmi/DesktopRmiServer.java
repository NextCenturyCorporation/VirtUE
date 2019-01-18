package com.ncc.savior.desktop.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.defaultApplications.RmiServer;
import com.ncc.savior.desktop.sidebar.Sidebar;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DesktopRmiServer extends UnicastRemoteObject implements DesktopRmiInterface {
	private static final String SERVICE_NAME = "SaviorDesktop";
	public static final String RMI_NAME = "//localhost/" + SERVICE_NAME;
	private static final Logger logger = LoggerFactory.getLogger(RmiServer.class);
	private static final long serialVersionUID = 1L;
	// registry needed to prevent it from being garbage collected.
	@SuppressWarnings("unused")
	private static Registry registry;
	private VirtueService virtueService;
	private Sidebar sidebar;

	protected DesktopRmiServer(VirtueService virtueService, Sidebar sidebar) throws RemoteException {
		super();
		this.virtueService = virtueService;
		this.sidebar = sidebar;
		bindServer(virtueService, sidebar);
	}

	public static void bindServer(VirtueService virtueService, Sidebar sidebar) {
		try {
			registry = LocateRegistry.createRegistry(1099);
			Naming.rebind(RMI_NAME, new DesktopRmiServer(virtueService, sidebar));
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
	public void open(String[] args) throws RemoteException {
		String virtueId = args[0];
		String applicationId = args[1];
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				boolean virtueFound = false;
				while (!virtueFound) {
					Map<String, DesktopVirtue> virtueMap = sidebar.getVirtueMap();
					DesktopVirtue virtue = virtueMap.get(virtueId);
					if (virtue != null) {
						logger.info("WE MADE IT");
						ApplicationDefinition appDefn = virtue.getApps().get(applicationId);
						try {
							// virtueService.startApplication(virtue, appDefn);
							virtueFound = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						logger.info("null virtue");
					}
				}
			}

		}, "remote application start thread");
		thread.start();
	}

	public static void main(String[] args) {
		bindServer(null, null);
	}

}
