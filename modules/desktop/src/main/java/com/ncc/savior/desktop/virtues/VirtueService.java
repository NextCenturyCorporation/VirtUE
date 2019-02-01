package com.ncc.savior.desktop.virtues;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.alerting.VirtueAlertMessage;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.rdp.IRdpClient;
import com.ncc.savior.desktop.sidebar.ColorManager;
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
	public static ArrayList<VirtueState> startableVirtueStates;
	public static ArrayList<VirtueState> stopableVirtueStates;
	private Map<String, List<ApplicationDefinition>> pendingApps;
	private IRdpClient rdpClient;
	private IClipboardManager clipboardManager;
	protected AuthorizationService authorizationService;
	private ColorManager colorManager;
	private HashMap<String, Object> locks;

	static {
		startableVirtueStates = new ArrayList<VirtueState>();
		stopableVirtueStates = new ArrayList<VirtueState>();
		startableVirtueStates.add(VirtueState.STOPPED);
		// startableVirtueStates.add(VirtueState.STOPPING);
		stopableVirtueStates.add(VirtueState.LAUNCHING);
		stopableVirtueStates.add(VirtueState.RUNNING);
	}

	public VirtueService(DesktopResourceService desktopResourceService, IApplicationManagerFactory appManger,
			IRdpClient rdpClient, IClipboardManager clipboardManager, AuthorizationService authService,
			ColorManager colorManager, boolean packetDebug) {
		this.desktopResourceService = desktopResourceService;
		this.connectionManager = new XpraConnectionManager(appManger, packetDebug);
		this.pendingApps = Collections.synchronizedMap(new HashMap<String, List<ApplicationDefinition>>());
		this.rdpClient = rdpClient;
		this.clipboardManager = clipboardManager;
		this.authorizationService = authService;
		this.colorManager = colorManager;
		this.locks = new HashMap<String, Object>();
	}

	public void ensureConnectionForVirtue(DesktopVirtue virtue) {
		Runnable runnable = () -> {
			Collection<DesktopVirtueApplication> apps = desktopResourceService.getReconnectionApps(virtue.getId());
			RgbColor color = getColorFromVirtue(virtue);
			apps.parallelStream().forEach((app) -> {
				try {
					ensureConnection(app, virtue, color);
				} catch (IOException e) {
					logger.error("Error creating connection to app=" + app + " virtue=" + virtue, e);
					BaseAlertMessage alertMessage = new VirtueAlertMessage(
							"Error connecting to virtue " + virtue.getName(), virtue,
							"Error connecting to virtue " + virtue.getName());
					UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
				}
			});
		};
		Thread t = new Thread(runnable, "Temp-reconnect-thread");
		t.start();
	}

	private RgbColor getColorFromVirtue(DesktopVirtue virtue) {
		Color bodyColor = colorManager.getBodyColor(virtue.getTemplateId());
		RgbColor color = RgbColor.fromColor(bodyColor);
		return color;
	}

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
		// if (OS.LINUX.equals(app.getOs())) {
		ensureConnectionLinux(app, virtue, color);
		// } else {
		// ensureConnectionWindows(app, virtue, color);
		// }
	}

	private void ensureConnectionWindows(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		String clipboardId = null;
		// for now we want to use the RDP clipboard bridge instead of our own app.
		boolean userRdpClientClipboardBridge = true;
		if (!userRdpClientClipboardBridge) {
			try {
				String key = app.getPrivateKey();
				SshConnectionParameters params = getConnectionParams(app, key);
				// For now user
				clipboardId = clipboardManager.connectClipboard(params, virtue.getName(), virtue.getTemplateId());
			} catch (IOException e) {
				logger.error("Failed to connect clipboard", e);
			}
		}
		Process p = rdpClient.startRdp(app, virtue, color);
		if (clipboardId != null) {
			// making the variable effectively final
			String cId = clipboardId;
			Thread t = new Thread(() -> {
				try {
					p.waitFor();
					clipboardManager.closeConnection(cId);
				} catch (InterruptedException | IOException e) {
					logger.error("Error tracking RDP connection and closing associated clipboard", e);
				} finally {

				}

			}, "RemoteDesktop-checker");
			t.setDaemon(true);
			t.start();
		}
	}

	private void ensureConnectionLinux(DesktopVirtueApplication app, DesktopVirtue virtue, RgbColor color)
			throws IOException {
		File file = null;
		try {
			String key = app.getPrivateKey();

			SshConnectionParameters params = getConnectionParams(app, key);
			String colorDesc = (color == null ? "" : " with color " + color.toString());
			logger.debug("verifying connection to " + app.getHostname() + colorDesc);
			// synchronized by virtue prevents user from clicking the application button
			// twice and getting 2 connections.
			Object lock = getLock(virtue);
			synchronized (lock) {
				XpraClient client = connectionManager.getExistingClient(params);
				if (client == null || client.getStatus() == Status.ERROR) {
					logger.debug("needed new connection");
					try {
						connectionManager.createXpraServerAndAddDisplayToParams(params);
						if (app.getOs().equals(OS.LINUX)) {
							logger.debug("connecting clipboard");
							clipboardManager.connectClipboard(params, virtue.getName(), virtue.getTemplateId());
						}
					} catch (IOException e) {
						logger.error("clipboard manager connection failed!", e);
						VirtueAlertMessage pam = new VirtueAlertMessage("Clipboard Failed", virtue,
								"Failed to connect clipboard to virtue");
						UserAlertingServiceHolder.sendAlertLogError(pam, logger);
					}
					client = connectionManager.createClient(params, color, virtue);
				}
			}
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
	}

	private synchronized Object getLock(DesktopVirtue virtue) {
		String key = virtue.getTemplateId();
		Object lock = locks.get(key);
		if (lock == null) {
			lock = new Object();
			locks.put(key, lock);
		}
		return lock;
	}

	private SshConnectionParameters getConnectionParams(DesktopVirtueApplication app, String key) throws IOException {
		SshConnectionParameters params = null;
		if (key != null && key.contains("BEGIN RSA PRIVATE KEY")) {
			params = SshConnectionParameters.withPemString(app.getHostname(), app.getPort(), app.getUserName(), key);
		} else {
			params = SshConnectionParameters.withPassword(app.getHostname(), app.getPort(), app.getUserName(), key);
		}
		return params;
	}

	/**
	 * Returns all the virtues for a user. If they are created (or in process of
	 * creating, they should have an id)
	 *
	 * @return
	 * @throws IOException
	 * @throws UserLoggedOutException
	 */
	public List<DesktopVirtue> getVirtuesForUser() throws IOException, UserLoggedOutException {
		List<DesktopVirtue> list = null;
		list = desktopResourceService.getVirtues();
		for (DesktopVirtue virtue : list) {
			List<ApplicationDefinition> pending = pendingApps.get(virtue.getId());
			if (VirtueState.RUNNING.equals(virtue.getVirtueState()) && pending != null && !pending.isEmpty()) {
				RgbColor color = getColorFromVirtue(virtue);
				Iterator<ApplicationDefinition> itr = pending.iterator();
				while (itr.hasNext()) {
					ApplicationDefinition appDefn = itr.next();
					logger.debug("starting pending application=" + appDefn);
					// moved such that if connection fails, we don't retry forever starting a ton of
					// applications
					itr.remove();
					Thread t = new Thread(() -> {
						try {
							DesktopVirtueApplication app = desktopResourceService.startApplication(virtue, appDefn);
							ensureConnection(app, virtue, color);
						} catch (Exception e) {
							logger.error("error starting pending application", e);
							BaseAlertMessage alertMessage = new VirtueAlertMessage(
									"Failed to start application " + appDefn.getName(), virtue,
									"Failed to start application " + appDefn.getName() + " for virtue "
											+ virtue.getName());
							UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
						}
					});
					t.start();
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
	public void startApplication(DesktopVirtue v, ApplicationDefinition appDefn) throws IOException {
		// TODO check to see if we have an XPRA connection
		RgbColor color = getColorFromVirtue(v);
		Thread t = new Thread(() -> {
			DesktopVirtue virtue = v;
			try {
				String virtueId = virtue.getId();
				DesktopVirtueApplication app;
				if (virtueId == null) {
					virtue = desktopResourceService.createVirtue(virtue.getTemplateId());
					addPendingAppStart(virtue.getId(), appDefn);
					// Set old object with new status
					v.setId(virtue.getId());
					v.setVirtueState(virtue.getVirtueState());
				} else {
					if (VirtueState.RUNNING.equals(virtue.getVirtueState())) {
						app = desktopResourceService.startApplication(virtue, appDefn);
						ensureConnection(app, virtue, color);
					} else if (VirtueState.STOPPED.equals(virtue.getVirtueState())) {
						startVirtue(virtue);
						addPendingAppStart(virtue.getId(), appDefn);
					} else {
						addPendingAppStart(virtue.getId(), appDefn);
					}
				}
			} catch (Throwable e) {
				logger.error("Error starting application", e);

				BaseAlertMessage alertMessage = new VirtueAlertMessage(
						"Failed to start application " + appDefn.getName(), v,
						"Failed to start application " + appDefn.getName() + " for virtue " + v.getName());
				UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
			}
		});
		t.start();
	}

	private void addPendingAppStart(String virtueId, ApplicationDefinition appDefn) {
		logger.debug("Adding pending application=" + appDefn);
		List<ApplicationDefinition> apps = pendingApps.get(virtueId);
		if (apps == null) {
			apps = new ArrayList<ApplicationDefinition>();
			pendingApps.put(virtueId, apps);
		}
		apps.add(appDefn);
	}

	// TODO Arguable, this could be handled differently.
	// * Ignore non-startable states
	// * Throw an Exception for non-startable states
	// * Allow non-startable states to follow through (maybe the user knows
	// something) and allow the server to return errors
	public void startVirtue(DesktopVirtue virtue) throws InvalidUserLoginException, IOException {
		if (startableVirtueStates.contains(virtue.getVirtueState())) {
			desktopResourceService.startVirtue(virtue.getId());
		}
	}

	// TODO see notes above and apply to this
	public void stopVirtue(DesktopVirtue virtue) throws InvalidUserLoginException, IOException {
		if (stopableVirtueStates.contains(virtue.getVirtueState())) {
			desktopResourceService.stopVirtue(virtue.getId());
		}
	}

	public void terminateVirtue(DesktopVirtue virtue) throws InvalidUserLoginException, IOException {
		if (virtue.getVirtueState() != VirtueState.UNPROVISIONED) {
			desktopResourceService.terminateVirtue(virtue.getId());
		}
	}

	public List<DesktopVirtue> getApplicationsWithTag(String tag) {
		return desktopResourceService.getApplicationsWithTag(tag);
	}

	public void closeXpraConnections() {
		connectionManager.closeActiveClients();
	}
}
