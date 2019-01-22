package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.alerting.VirtueAlertMessage;
import com.ncc.savior.desktop.clipboard.hub.IDefaultApplicationListener;
import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.desktop.sidebar.defaultapp.DefaultAppTableDialog;
import com.ncc.savior.desktop.sidebar.defaultapp.IAppChooser;
import com.ncc.savior.desktop.sidebar.defaultapp.StatusFirstVirtueAppComparator;
import com.ncc.savior.desktop.sidebar.prefs.DesktopPreference;
import com.ncc.savior.desktop.sidebar.prefs.PreferenceService;
import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Handles actually launching the default application.
 *
 */
public class DefaultApplicationLauncher implements IDefaultApplicationListener {
	private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationLauncher.class);
	private static final String PREF_KEY_VIRTUE_TEMPLATE_ID = "virtueId";
	private static final String PREF_KEY_APP_ID = "appId";
	private VirtueService virtueService;
	private IIconService iconService;
	private ColorManager colorManager;
	private PreferenceService preferenceService;
	private Set<IHyperlinkMessageListener> hyperlinkMessageListeners;

	public DefaultApplicationLauncher(VirtueService virtueService,
			IIconService iconService, ColorManager colorManager, PreferenceService preferenceService) {
		this.virtueService = virtueService;
		this.iconService = iconService;
		this.colorManager = colorManager;
		this.preferenceService = preferenceService;
		this.hyperlinkMessageListeners = new HashSet<IHyperlinkMessageListener>();
	}

	@Override
	public void activateDefaultApp(DefaultApplicationType defaultApplicationType, List<String> arguments,
			String sourceId) {
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
			startAppWithParam(pair, params, sourceId);
		} else {
			startAppFromUserSelection(defaultApplicationType, possibleApps, params, sourceId);
		}

	}

	private void startAppWithParam(Pair<DesktopVirtue, ApplicationDefinition> pair, String params, String sourceId) {
		logger.debug("Starting application on " + pair);

		try {
			// desktopResourceService.startApplication(pair.getLeft(), pair.getRight(),
			// params);
			ApplicationDefinition app = pair.getRight();
			app.setParameters(params);
			virtueService.startApplication(pair.getLeft(), app);
			triggerHyperlinkMessage(sourceId, pair.getLeft().getTemplateId(), DefaultApplicationType.BROWSER, params);
		} catch (IOException e) {
			logger.error("Error starting application", e);
		}
	}

	private Pair<DesktopVirtue, ApplicationDefinition> getSavedVirtueIdAndApplicationDefn(
			DefaultApplicationType defaultApplicationType, List<DesktopVirtue> possibleApps) {
		// try {
		Preferences favorites = preferenceService.getPreferenceNode(DesktopPreference.DEFAULT_APPS,
				defaultApplicationType.toString());
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
		// } catch (Exception e) {
		// String msg = "Unable to get valid user to get saved default Application for "
		// + defaultApplicationType.toString();
		// logAndAlertError(e, "Unable to get user", msg);
		// }
		// return null;
	}

	protected void logAndAlertError(Exception e, String title, String msg) {
		logger.error(msg, e);
		BaseAlertMessage alertMessage = new PlainAlertMessage(title, msg);
		UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
	}

	private void startAppFromUserSelection(DefaultApplicationType defaultApplicationType,
			List<DesktopVirtue> possibleApps, String params, String sourceId) {
		Vector<Pair<DesktopVirtue, ApplicationDefinition>> comboList = new Vector<Pair<DesktopVirtue, ApplicationDefinition>>();
		for (DesktopVirtue v : possibleApps) {
			for (ApplicationDefinition a : v.getApps().values()) {
				comboList.add(Pair.of(v, a));
			}
		}
		comboList.sort(new StatusFirstVirtueAppComparator());

		// IAppChooser dald = new DefaultAppListDialog(iconService);
		IAppChooser dald = new DefaultAppTableDialog(iconService, colorManager);
		dald.setVirtueAppChoices(comboList);
		dald.setParameters(params);
		dald.setAppType(defaultApplicationType);
		dald.setStartAppBiConsumer((pair, ps) -> {
			startAppWithParam(pair, ps, sourceId);
		});

		dald.setSavePreferenceAction((pair) -> {
			try {
				Preferences defaultApp = preferenceService.getPreferenceNode(DesktopPreference.DEFAULT_APPS,
						defaultApplicationType.toString());
				defaultApp.put(PREF_KEY_VIRTUE_TEMPLATE_ID, pair.getLeft().getTemplateId());
				defaultApp.put(PREF_KEY_APP_ID, pair.getRight().getId());
			} catch (Exception e1) {
				String msg = "Unable to save default Application for " + defaultApplicationType.toString();
				logAndAlertError(e1, "Error writing preferences ", msg);
			}
		});

		dald.start();

	}

	public void addHyperlinkMessageListener(IHyperlinkMessageListener listener) {
		hyperlinkMessageListeners.add(listener);
	}

	public void triggerHyperlinkMessage(String dataSourceGroupId, String dataDestinationGroupId,
			DefaultApplicationType applicationType, String params) {
		for (IHyperlinkMessageListener listener : hyperlinkMessageListeners) {
			listener.onMessage(dataSourceGroupId, dataDestinationGroupId, applicationType, params);
		}
	}

	public static interface IHyperlinkMessageListener {

		public void onMessage(String dataSourceGroupId, String dataDestinationGroupId,
				DefaultApplicationType applicationType, String params);

	}
}
