package com.ncc.savior.desktop.virtues;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.sidebar.RgbColor;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.XpraClient.Status;
import com.ncc.savior.desktop.xpra.XpraConnectionManager;
import com.ncc.savior.desktop.xpra.connection.ssh.SshConnectionFactory.SshConnectionParameters;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtueApplication;

/**
 * Service for handling virtues from the Desktop application. This service has
 * limited functions because the Desktop needs only limited functions at this
 * point. This service will get the current state of a users virtues and handle
 * starting of applications. If an application is requested to be started and a
 * virtue is not ready, this service will save that and start the application
 * when the virtue is ready.
 *
 *
 */
public class VirtueService {
	private static final Logger logger = LoggerFactory.getLogger(VirtueService.class);
	private XpraConnectionManager connectionManager;
	private DesktopResourceService desktopResourceService;
	private Map<String, List<ApplicationDefinition>> pendingApps;
	private Map<String, RgbColor> colors;

	public VirtueService(DesktopResourceService desktopResourceService, IApplicationManagerFactory appManger) {
		this.desktopResourceService = desktopResourceService;
		this.connectionManager = new XpraConnectionManager(appManger);
		this.pendingApps = Collections.synchronizedMap(new HashMap<String, List<ApplicationDefinition>>());
		this.colors = Collections.synchronizedMap(new HashMap<String, RgbColor>());
	}

	// public void connectAndStartApp(DesktopVirtue app) throws IOException {
	// IConnectionParameters params = app.getConnectionParams();
	// // Do we have an existing client/connection for this params? If so, start a
	// new
	// // app and be done with it.
	//
	// // If we don't have an existing client/connection, then we need to create a
	// // connection and then start the app.
	// XpraClient client = connectionManager.getExistingClient(params);
	// if (client == null || client.getStatus() == Status.ERROR) {
	// client = connectionManager.createClient(params);
	// }
	// connectionManager.startApplication(params, app.getStartCommand());
	// }

	/**
	 * Starts a connection for a particularly {@link DesktopVirtueApplication} if
	 * necessary. If there is one already, this function won't do anything.
	 *
	 * @param app
	 * @param virtue
	 * @param color
	 * @throws IOException
	 */
	public void ensureConnection(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		if (OS.LINUX.equals(app.getOs())) {
			ensureConnectionLinux(app, virtue, color);
		} else {
			ensureConnectionWindows(app, virtue, color);
		}
	}

	private void ensureConnectionWindows(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		WindowsRdp.startRdp(app, virtue, color);
	}

	private void ensureConnectionLinux(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		File file = null;
		try {
			String key = app.getPrivateKey();

			SshConnectionParameters params = null;
			if (key != null && key.contains("BEGIN RSA PRIVATE KEY")) {
				File pem = File.createTempFile(app.getName(), ".pem");
				FileWriter writer = new FileWriter(pem);
				writer.write(key);
				writer.close();
				params = new SshConnectionParameters(app.getHostname(), app.getPort(), app.getUserName(), pem);
			} else {
				params = new SshConnectionParameters(app.getHostname(), app.getPort(), app.getUserName(), key);
			}
			String colorDesc = (color == null ? "" : " with color " + color.toString());
			logger.debug("verifying connection to " + app.getHostname() + colorDesc);
			XpraClient client = connectionManager.getExistingClient(params);
			if (client == null || client.getStatus() == Status.ERROR) {
				logger.debug("needed new connection");
				client = connectionManager.createClient(params, color);
			}
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * Returns all the virtues for a user. If they are created (or in process of
	 * creating, they should have an id)
	 *
	 * @return
	 * @throws IOException
	 */
	public List<DesktopVirtue> getVirtuesForUser() throws IOException {
		List<DesktopVirtue> list = null;
		list = desktopResourceService.getVirtues();
		for (DesktopVirtue virtue : list) {
			List<ApplicationDefinition> pending = pendingApps.get(virtue.getId());
			if (VirtueState.RUNNING.equals(virtue.getVirtueState()) && pending != null && !pending.isEmpty()) {
				RgbColor color = colors.get(virtue.getId());
				Iterator<ApplicationDefinition> itr = pending.iterator();
				while (itr.hasNext()) {
					ApplicationDefinition appDefn = itr.next();
					logger.debug("starting pending application=" + appDefn);
					DesktopVirtueApplication app = desktopResourceService.startApplication(virtue.getId(), appDefn);
					ensureConnection(app, virtue, color);
					itr.remove();
				}
			}
		}
		return list;
	}

	/**
	 * Starts an application or starts the virtue associated for the application and
	 * marks the application to be started once the virtue is ready.
	 *
	 * @param virtue
	 * @param appDefn
	 * @param color
	 * @throws IOException
	 */
	public void startApplication(DesktopVirtue virtue, ApplicationDefinition appDefn, RgbColor color)
			throws IOException {
		// TODO check to see if we have an XPRA connection
		String virtueId = virtue.getId();
		DesktopVirtueApplication app;
		if (virtueId == null) {
			virtue = desktopResourceService.startVirtue(virtue.getTemplateId());
			addPendingAppStart(virtue.getId(), appDefn, color);
		} else {
			if (VirtueState.RUNNING.equals(virtue.getVirtueState())) {
				app = desktopResourceService.startApplication(virtueId, appDefn);
				ensureConnection(app, virtue, color);
			} else {
				addPendingAppStart(virtue.getId(), appDefn, color);
			}
		}

	}

	private void addPendingAppStart(String virtueId, ApplicationDefinition appDefn, RgbColor color) {
		logger.debug("Adding pending application=" + appDefn);
		List<ApplicationDefinition> apps = pendingApps.get(virtueId);
		if (apps == null) {
			apps = new ArrayList<ApplicationDefinition>();
			pendingApps.put(virtueId, apps);
			colors.put(virtueId, color);
		}
		apps.add(appDefn);
	}
}
