package com.ncc.savior.desktop.sidebar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.prefs.BackingStoreException;
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
import com.ncc.savior.desktop.clipboard.hub.IDefaultApplicationListener;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DefaultApplicationLauncher implements IDefaultApplicationListener {
	private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationLauncher.class);
	private static final String PREF_KEY_VIRTUE_TEMPLATE_ID = "virtueId";
	private static final String PREF_KEY_APP_ID = "appId";
	private AuthorizationService authorizationService;
	private VirtueService virtueService;

	public DefaultApplicationLauncher(AuthorizationService authService, VirtueService virtueService) {
		this.authorizationService = authService;
		this.virtueService = virtueService;
	}

	@Override
	public void activateDefaultApp(DefaultApplicationType defaultApplicationType, List<String> arguments) {
		// TODO probably need to thread this
		List<DesktopVirtue> possibleApps = virtueService.getApplicationsWithTag(defaultApplicationType.toString());
		logger.debug("list of apps(" + possibleApps.size() + "): " + possibleApps);
		String params = "";
		for (String arg : arguments) {
			params += arg + " ";
		}
		Pair<DesktopVirtue, ApplicationDefinition> pair = getSavedVirtueIdAndApplicationDefn(defaultApplicationType,
				possibleApps);
		if (pair != null) {
			VirtueAlertMessage vam = new VirtueAlertMessage("Starting cross-virtue application from user preference",
					pair.getLeft(), "Starting cross-virtue application, " + pair.getRight().getName()
							+ ", using virtue, " + pair.getLeft().getName() + ", as indicated from user preference.");
			UserAlertingServiceHolder.sendAlertLogError(vam, logger);
			startAppWithParam(pair, params);
		} else {
			startAppFromUserSelection(defaultApplicationType, possibleApps, params);
		}

	}

	public void clearPrefs() {
		try {
			DesktopUser user = authorizationService.getUser();
			Preferences defaultAppsPrefs = Preferences.userRoot()
					.node("VirtUE/Desktop/" + user.getUsername() + "/defaultApps");
			defaultAppsPrefs.removeNode();
			// String[] apps = defaultAppsPrefs.childrenNames();
			// for (String app : apps) {
			// Preferences appPref = Preferences.userRoot()
			// .node("VirtUE/Desktop/" + user.getUsername() + "/defaultApps/" + app);
			// appPref.clear();
			// appPref.rem
			// }
		} catch (InvalidUserLoginException e) {
			String msg = "Unable to clear default application preferences because username is invalid.";
			logAndAlertError(e, "Unable to get user", msg);
		} catch (BackingStoreException e) {
			String msg = "Unable to clear default application preferences because unable to find preferences.";
			logAndAlertError(e, "Unable to get user", msg);
		}

	}

	private void startAppWithParam(Pair<DesktopVirtue, ApplicationDefinition> pair, String params) {
		logger.debug("Starting application on " + pair);

		try {
			// desktopResourceService.startApplication(pair.getLeft(), pair.getRight(),
			// params);
			ApplicationDefinition app = pair.getRight();
			app.setParameters(params);
			virtueService.startApplication(pair.getLeft(), app, null);
		} catch (IOException e) {
			logger.error("Error starting application", e);
		}
	}

	private Pair<DesktopVirtue, ApplicationDefinition> getSavedVirtueIdAndApplicationDefn(
			DefaultApplicationType defaultApplicationType, List<DesktopVirtue> possibleApps) {
		DesktopUser user;
		try {
			user = authorizationService.getUser();

			Preferences favorites = Preferences.userRoot()
					.node("VirtUE/Desktop/" + user.getUsername() + "/defaultApps/" + defaultApplicationType.toString());
			String virtueTemplateId = favorites.get(PREF_KEY_VIRTUE_TEMPLATE_ID, null);
			String appId = favorites.get(PREF_KEY_APP_ID, null);
			if (virtueTemplateId == null || appId == null) {
				return null;
			} else {
				for (DesktopVirtue v : possibleApps) {
					if (virtueTemplateId.equals(v.getTemplateId())) {
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
			String msg = "Unable to get valid user to get saved default Application for "
					+ defaultApplicationType.toString();
			logAndAlertError(e, "Unable to get user", msg);
		}
		return null;
	}

	protected void logAndAlertError(Exception e, String title, String msg) {
		logger.error(msg, e);
		BaseAlertMessage alertMessage = new PlainAlertMessage(title, msg);
		UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
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

				@SuppressWarnings("unchecked")
				Pair<DesktopVirtue, ApplicationDefinition> pair = (Pair<DesktopVirtue, ApplicationDefinition>) combo
						.getSelectedItem();
				DesktopUser user;
				try {
					if (saveCheckbox.isSelected()) {
						user = authorizationService.getUser();
						Preferences favorites = Preferences.userRoot().node("VirtUE/Desktop/" + user.getUsername()
								+ "/defaultApps/" + defaultApplicationType.toString());
						favorites.put(PREF_KEY_VIRTUE_TEMPLATE_ID, pair.getLeft().getTemplateId());
						favorites.put(PREF_KEY_APP_ID, pair.getRight().getId());
					}
				} catch (Exception e1) {
					String msg = "Unable to save default Application for "
							+ defaultApplicationType.toString();
					logAndAlertError(e1, "Error writing preferences ", msg);
				}
				startAppWithParam(pair, params);
				dialog.dispose();
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
