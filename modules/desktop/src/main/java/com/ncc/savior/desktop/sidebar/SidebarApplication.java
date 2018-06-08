package com.ncc.savior.desktop.sidebar;

import java.io.File;

import com.ncc.savior.configuration.PropertyManager;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.clipboard.IClipboardManager;
import com.ncc.savior.desktop.clipboard.connection.SshClipboardManager;
import com.ncc.savior.desktop.clipboard.guard.ConstantDataGuard;
import com.ncc.savior.desktop.clipboard.hub.ClipboardHub;
import com.ncc.savior.desktop.rdp.FreeRdpClient;
import com.ncc.savior.desktop.rdp.IRdpClient;
import com.ncc.savior.desktop.rdp.WindowsRdp;
import com.ncc.savior.desktop.virtues.DesktopResourceService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.application.swing.SwingApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxXpraKeyMap;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyMap;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;
import com.ncc.savior.util.JavaUtil;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is the main class to start just the sidebar application. This may or may
 * not end up being the starting point for the Desktop application.
 *
 *
 */
public class SidebarApplication extends Application {
	public static void main(String[] args) {
		JavaUtil.startMemLogger(1000);
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		// Plumbing and depedency injection
		PropertyManager props = PropertyManager.defaultPropertyLocations(true);
		String baseUrl = props.getString(PropertyManager.PROPERTY_BASE_API_PATH);
		String desktopUrl = baseUrl + props.getString(PropertyManager.PROPERTY_DESKTOP_API_PATH);
		String loginUrl = baseUrl + props.getString(PropertyManager.PROPERTY_LOGIN_API_PATH);
		String logoutUrl = baseUrl + props.getString(PropertyManager.PROPERTY_LOGOUT_API_PATH);
		String requiredDomain = props.getString(PropertyManager.PROPERTY_REQUIRED_DOMAIN);
		String freerdpPath = props.getString(PropertyManager.PROPERTY_FREERDP_PATH);
		boolean dummyAuthorization = props.getBoolean(PropertyManager.PROPERTY_DUMMY_AUTHORIZATION, false);
		boolean allowInsecureSsl = props.getBoolean(PropertyManager.PROPERTY_ALLOW_INSECURE_SSL, false);
		boolean useColors = props.getBoolean(PropertyManager.PROPERTY_USE_COLORS, false);
		boolean swing = props.getBoolean(PropertyManager.PROPERTY_SWING, true);
		String style = props.getString(PropertyManager.PROPERTY_STYLE);
		AuthorizationService authService = new AuthorizationService(requiredDomain, dummyAuthorization, loginUrl,
				logoutUrl);
		DesktopResourceService drs = new DesktopResourceService(authService, desktopUrl, allowInsecureSsl);
		IApplicationManagerFactory appManager;
		if (swing) {
			appManager = new SwingApplicationManagerFactory(new SwingKeyboard(new SwingKeyMap()));
		} else {
			appManager = new JavaFxApplicationManagerFactory(new JavaFxKeyboard(new JavaFxXpraKeyMap()));
		}
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

		ClipboardHub clipboardHub = new ClipboardHub(new ConstantDataGuard(true));
		String sourceJarPath = "C:\\projects\\VirtUE\\workspace\\VirtUE\\modules\\clipboard\\build\\libs\\clipboard-0.1.0-SNAPSHOT-all.jar";
		IClipboardManager clipboardManager = new SshClipboardManager(clipboardHub, sourceJarPath);
		VirtueService virtueService = new VirtueService(drs, appManager, rdpClient, clipboardManager);
		Sidebar sidebar = new Sidebar(virtueService, authService, useColors, style);
		SidebarController controller = new SidebarController(virtueService, sidebar, authService);
		controller.init(primaryStage);
	}
}
