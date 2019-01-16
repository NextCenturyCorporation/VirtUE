package com.ncc.savior.desktop.sidebar;

import java.awt.HeadlessException;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;

import com.ncc.savior.configuration.PropertyManager;
import com.ncc.savior.desktop.alerting.ToastUserAlertService;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.connection.SshClipboardManager;
import com.ncc.savior.desktop.clipboard.guard.CopyPasteDialog;
import com.ncc.savior.desktop.clipboard.guard.ICrossGroupDataGuard;
import com.ncc.savior.desktop.clipboard.guard.IDataGuardDialog;
import com.ncc.savior.desktop.clipboard.guard.RestDataGuard;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.rdp.FreeRdpClient;
import com.ncc.savior.desktop.rdp.IRdpClient;
import com.ncc.savior.desktop.rdp.WindowsRdp;
import com.ncc.savior.desktop.sidebar.prefs.PreferenceService;
import com.ncc.savior.desktop.virtues.BridgeSensorService;
import com.ncc.savior.desktop.virtues.DesktopResourceService;
import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.desktop.virtues.IconResourceService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.application.swing.SwingApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyMap;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;

/**
 * This is the main class to start just the sidebar application. This may or may
 * not end up being the starting point for the Desktop application.
 *
 *
 */
public class SidebarApplication {
	public static void main(String[] args) throws HeadlessException, Exception {
		start(new JFrame());
	}

	public static void start(JFrame primaryFrame) throws Exception {

		// Plumbing and dependency injection
		PropertyManager props = PropertyManager.defaultPropertyLocations(true);
		URL baseUrl = new URL(props.getString(PropertyManager.PROPERTY_BASE_API_PATH));
		URL desktopUrl = new URL(baseUrl, props.getString(PropertyManager.PROPERTY_DESKTOP_API_PATH));
		URL loginUrl = new URL(baseUrl, props.getString(PropertyManager.PROPERTY_LOGIN_API_PATH));
		URL logoutUrl = new URL(baseUrl, props.getString(PropertyManager.PROPERTY_LOGOUT_API_PATH));
		String requiredDomain = props.getString(PropertyManager.PROPERTY_REQUIRED_DOMAIN);
		String freerdpPath = props.getString(PropertyManager.PROPERTY_FREERDP_PATH);
		boolean allowInsecureSsl = props.getBoolean(PropertyManager.PROPERTY_ALLOW_INSECURE_SSL, false);
		boolean packetDebug = props.getBoolean(PropertyManager.PROPERTY_PACKET_DEBUG, false);
		long bridgeSensorTimeoutMillis = props.getLong(PropertyManager.PROPERTY_BRIDGE_SENSOR_TIMEOUT_MILLIS, 5000);
		int port = props.getInt(PropertyManager.PROPERTY_BRIDGE_SENSOR_PORT, 8080);
		String host = props.getString(PropertyManager.PROPERTY_BRIDGE_SENSOR_HOST);
		// boolean useColors = props.getBoolean(PropertyManager.PROPERTY_USE_COLORS,
		// false);
		// String style = props.getString(PropertyManager.PROPERTY_STYLE);
		String sourceJarPath = props.getString(PropertyManager.PROPERTY_CLIPBOARD_JAR_PATH);

		long dataGuardAskStickyTimeoutMillis = props.getLong(PropertyManager.PROPERTY_CLIPBOARD_ASK_TIMEOUT_MILLIS,
				1000 * 60 * 15);

		long alertPersistTimeMillis = props.getLong(PropertyManager.PROPERTY_ALERT_PERSIST_TIME, 3000);

		AuthorizationService authService = new AuthorizationService(requiredDomain, loginUrl.toString(),
				logoutUrl.toString());
		BridgeSensorService bridgeSensorService = new BridgeSensorService(bridgeSensorTimeoutMillis, port, host, false);
		DesktopResourceService drs = new DesktopResourceService(authService, desktopUrl.toString(), allowInsecureSsl,
				bridgeSensorService);
		IApplicationManagerFactory appManager;
		appManager = new SwingApplicationManagerFactory(new SwingKeyboard(new SwingKeyMap()));
		File freerdpExe = null;
		if (freerdpPath != null) {
			freerdpExe = new File(freerdpPath);
		}
		IRdpClient rdpClient;
		if (freerdpExe != null && freerdpExe.exists()) {
			rdpClient = new FreeRdpClient(freerdpExe);
		} else {
			rdpClient = new WindowsRdp();
		}

		IDataGuardDialog dialog = new CopyPasteDialog();
		ICrossGroupDataGuard dataGuard = new RestDataGuard(drs, dataGuardAskStickyTimeoutMillis, dialog);
		ClipboardHub clipboardHub = new ClipboardHub(dataGuard);
		UserAlertingServiceHolder.setAlertService(new ToastUserAlertService(alertPersistTimeMillis));
		IClipboardManager clipboardManager = new SshClipboardManager(clipboardHub, sourceJarPath);
		ColorManager colorManager = new ColorManager();
		IIconService iconService = new IconResourceService(drs);
		PreferenceService prefService = new PreferenceService(authService);
		VirtueService virtueService = new VirtueService(drs, appManager, rdpClient, clipboardManager, authService,
				colorManager, packetDebug);
		Sidebar sidebar = new Sidebar(virtueService, authService, iconService, colorManager, prefService,
				bridgeSensorService);
		SidebarController controller = new SidebarController(virtueService, sidebar, authService);
		clipboardHub.addDefaultApplicationListener(sidebar.getDefaultApplicationHandler());
		clipboardHub.addDataMessageListener(sidebar.getDataMessageListener());
		controller.init(primaryFrame);
	}
}
