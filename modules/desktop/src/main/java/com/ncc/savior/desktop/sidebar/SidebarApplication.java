package com.ncc.savior.desktop.sidebar;

import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFrame;

import com.ncc.savior.configuration.PropertyManager;
import com.ncc.savior.desktop.authorization.AuthorizationService;
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

		VirtueService virtueService = new VirtueService(drs, appManager, rdpClient);
		Sidebar sidebar = new Sidebar(virtueService, authService, useColors, style);
		SidebarController controller = new SidebarController(virtueService, sidebar, authService);
		controller.init(primaryFrame);
	}
}
