package com.ncc.savior.desktop.virtues;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.alerting.VirtueAlertMessage;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.authorization.InvalidUserLoginException;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.hub.IDefaultApplicationListener;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.desktop.rdp.IRdpClient;
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
	private Map<String, RgbColor> colors;
	private IRdpClient rdpClient;
	private IClipboardManager clipboardManager;
	private IDefaultApplicationListener defaultApplicationListener;
	protected AuthorizationService authorizationService;

	static {
		startableVirtueStates = new ArrayList<VirtueState>();
		stopableVirtueStates = new ArrayList<VirtueState>();
		startableVirtueStates.add(VirtueState.STOPPED);
		// startableVirtueStates.add(VirtueState.STOPPING);
		stopableVirtueStates.add(VirtueState.LAUNCHING);
		stopableVirtueStates.add(VirtueState.RUNNING);
	}

	public VirtueService(DesktopResourceService desktopResourceService, IApplicationManagerFactory appManger,
			IRdpClient rdpClient, IClipboardManager clipboardManager, AuthorizationService authService) {
		this.desktopResourceService = desktopResourceService;
		this.connectionManager = new XpraConnectionManager(appManger);
		this.pendingApps = Collections.synchronizedMap(new HashMap<String, List<ApplicationDefinition>>());
		this.colors = Collections.synchronizedMap(new HashMap<String, RgbColor>());
		this.rdpClient = rdpClient;
		this.clipboardManager = clipboardManager;
		this.defaultApplicationListener = new VirtueServiceDefaultApplicationListener();
		this.authorizationService = authService;
	}

	public void ensureConnectionForVirtue(DesktopVirtue virtue) {
		Runnable runnable = () -> {
			Collection<DesktopVirtueApplication> apps = desktopResourceService.getReconnectionApps(virtue.getId());
			RgbColor color = colors.get(virtue.getId());
			apps.parallelStream().forEach((app) -> {
				try {
					ensureConnection(app, virtue, color);
				} catch (IOException e) {
					logger.error("Error creating connection to app=" + app + " virtue=" + virtue);
				}
			});
		};
		Thread t = new Thread(runnable, "Temp-reconnect-thread");
		t.start();
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
		if (OS.LINUX.equals(app.getOs())) {
			ensureConnectionLinux(app, virtue, color);
		} else {
			ensureConnectionWindows(app, virtue, color);
		}
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
					logger.error("Error tracking RDP connection and closing associated clipboard");
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
			XpraClient client = connectionManager.getExistingClient(params);
			if (client == null || client.getStatus() == Status.ERROR) {
				logger.debug("needed new connection");
				try {
					connectionManager.createXpraServerAndAddDisplayToParams(params);
					logger.debug("connecting clipboard");
					clipboardManager.connectClipboard(params, virtue.getName(), virtue.getTemplateId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("clipboard manager connection failed!", e);
					UserAlertingServiceHolder.sendAlert(new VirtueAlertMessage("Clipboard Failed", virtue,
							"Failed to connect clipboard to virtue"));
				}
				client = connectionManager.createClient(params, color, virtue);
			}
		} finally {
			if (file != null && file.exists()) {
				file.delete();
			}
		}
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
					// moved such that if connection fails, we don't retry forever starting a ton of
					// applications
					itr.remove();
					Thread t = new Thread(() -> {
						try {
							DesktopVirtueApplication app = desktopResourceService.startApplication(virtue.getId(),
									appDefn);
							ensureConnection(app, virtue, color);
						} catch (Exception e) {
							logger.error("error starting pending application", e);
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
	public void startApplication(DesktopVirtue v, ApplicationDefinition appDefn, RgbColor color) throws IOException {
		// TODO check to see if we have an XPRA connection
		Thread t = new Thread(() -> {
			DesktopVirtue virtue = v;
			try {
				String virtueId = virtue.getId();
				DesktopVirtueApplication app;
				if (virtueId == null) {
					virtue = desktopResourceService.createVirtue(virtue.getTemplateId());
					addPendingAppStart(virtue.getId(), appDefn, color);
					// Set old object with new status
					v.setId(virtue.getId());
					v.setVirtueState(virtue.getVirtueState());
				} else {
					if (VirtueState.RUNNING.equals(virtue.getVirtueState())) {
						app = desktopResourceService.startApplication(virtueId, appDefn);
						ensureConnection(app, virtue, color);
					} else if (VirtueState.STOPPED.equals(virtue.getVirtueState())) {
						startVirtue(virtue);
						addPendingAppStart(virtue.getId(), appDefn, color);
					} else {
						addPendingAppStart(virtue.getId(), appDefn, color);
					}
				}
			} catch (Throwable e) {
				logger.error("Error starting application", e);
			}
		});
		t.start();
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

	public IDefaultApplicationListener getDefaultApplicationListener() {
		return this.defaultApplicationListener;
	}

	public class VirtueServiceDefaultApplicationListener implements IDefaultApplicationListener {

		private static final String PREF_KEY_VIRTUE_ID = "virtueId";
		private static final String PREF_KEY_APP_ID = "appId";

		@Override
		public void activateDefaultApp(DefaultApplicationType defaultApplicationType, List<String> arguments) {
			// TODO probably need to thread this
			List<DesktopVirtue> possibleApps = desktopResourceService
					.getApplicationsWithTag(defaultApplicationType.toString());
			logger.debug("list of apps(" + possibleApps.size() + "): " + possibleApps);
			String params = "";
			for (String arg : arguments) {
				params += arg + " ";
			}
			Pair<DesktopVirtue, ApplicationDefinition> pair = getSavedVirtueIdAndApplicationDefn(defaultApplicationType,
					possibleApps);
			if (pair != null) {
				startAppWithParam(pair, params);
			} else {
				startAppFromUserSelection(defaultApplicationType, possibleApps, params);
			}

		}

		private void startAppWithParam(Pair<DesktopVirtue, ApplicationDefinition> pair, String params) {
			logger.debug("Starting application on " + pair);

			try {
				// desktopResourceService.startApplication(pair.getLeft(), pair.getRight(),
				// params);
				ApplicationDefinition app = pair.getRight();
				app.setParameters(params);
				startApplication(pair.getLeft(), app, null);
			} catch (IOException e) {
				logger.error("Error starting application", e);
			}
		}

		private Pair<DesktopVirtue, ApplicationDefinition> getSavedVirtueIdAndApplicationDefn(
				DefaultApplicationType defaultApplicationType, List<DesktopVirtue> possibleApps) {
			DesktopUser user;
			try {
				user = authorizationService.getUser();

				Preferences favorites = Preferences.userRoot().node(
						"VirtUE/Desktop/" + user.getUsername() + "/defaultApps/" + defaultApplicationType.toString());
				String virtueId = favorites.get(PREF_KEY_VIRTUE_ID, null);
				String appId = favorites.get(PREF_KEY_APP_ID, null);
				if (virtueId == null || appId == null) {
					return null;
				} else {
					for (DesktopVirtue v : possibleApps) {
						if (virtueId.equals(v.getId())) {
							for (ApplicationDefinition a : v.getApps().values()) {
								if (appId.equals(a.getId())) {
									return Pair.of(v, a);
								}
							}
						}
					}
					return null;
				}
			} catch (InvalidUserLoginException e) {
				String msg = "Unable to get valid user to open default Application for "
						+ defaultApplicationType.toString();
				logger.error(msg, e);
				BaseAlertMessage alertMessage = new PlainAlertMessage("Unable to get user", msg);
				try {
					UserAlertingServiceHolder.sendAlert(alertMessage);
				} catch (IOException e1) {
					logger.debug("failed to send alert", e);
				}
			}
			return null;
		}

		private void startAppFromUserSelection(DefaultApplicationType defaultApplicationType,
				List<DesktopVirtue> possibleApps, String params) {

			JDialog dialog = new JDialog();
			dialog.setTitle("Open Browser");
			JScrollPane sp = new JScrollPane();
			sp.getVerticalScrollBar().setUnitIncrement(16);
			JPanel container = new JPanel();

			JComboBox<Pair<DesktopVirtue, ApplicationDefinition>> combo = new JComboBox<Pair<DesktopVirtue, ApplicationDefinition>>();
			for (DesktopVirtue v : possibleApps) {
				for (ApplicationDefinition a : v.getApps().values()) {
					combo.addItem(Pair.of(v, a));
				}
			}
			combo.setRenderer(new ListCellRenderer<Pair<DesktopVirtue, ApplicationDefinition>>() {

				@Override
				public Component getListCellRendererComponent(
						JList<? extends Pair<DesktopVirtue, ApplicationDefinition>> list,
						Pair<DesktopVirtue, ApplicationDefinition> value, int index, boolean isSelected,
						boolean cellHasFocus) {
					return new JLabel(value.getRight().getName() + " - " + value.getLeft().getName());
				}
			});

			JCheckBox saveCheckbox = new JCheckBox("Save a preference");
			JButton openButton = new JButton("Open");
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Pair<DesktopVirtue, ApplicationDefinition> pair = (Pair<DesktopVirtue, ApplicationDefinition>) combo
								.getSelectedItem();
						DesktopUser user;

						if (saveCheckbox.isSelected()) {
							user = authorizationService.getUser();
							Preferences favorites = Preferences.userRoot().node("VirtUE/Desktop/" + user.getUsername()
									+ "/defaultApps/" + defaultApplicationType.toString());
							favorites.put(PREF_KEY_VIRTUE_ID, pair.getLeft().getId());
							favorites.put(PREF_KEY_APP_ID, pair.getRight().getId());
						}
						startAppWithParam(pair, params);

						dialog.dispose();
					} catch (InvalidUserLoginException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});

			container.add(combo);
			container.add(saveCheckbox);
			container.add(openButton);
			sp.setViewportView(container);
			dialog.add(sp);
			dialog.setSize(new Dimension(400, 250));
			dialog.setVisible(true);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		}
	}
}
