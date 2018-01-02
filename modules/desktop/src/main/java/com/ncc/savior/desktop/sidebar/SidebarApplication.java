package com.ncc.savior.desktop.sidebar;

import com.ncc.savior.configuration.PropertyManager;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.virtues.DesktopResourceService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.desktop.xpra.IApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.application.javafx.JavaFxApplicationManagerFactory;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;

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
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Plumbing and depedency injection
		PropertyManager props = PropertyManager.defaultPropertyLocations(true);
		String desktopUrl = props.getString(PropertyManager.PROPERTY_DESKTOP_API_PATH);
		String requiredDomain = props.getString(PropertyManager.PROPERTY_REQUIRED_DOMAIN);
		boolean dummyAuthorization = props.getBoolean(PropertyManager.PROPERTY_DUMMY_AUTHORIZATION, false);
		boolean allowInsecureSsl = props.getBoolean(PropertyManager.PROPERTY_ALLOW_INSECURE_SSL, false);
		boolean useColors = props.getBoolean(PropertyManager.PROPERTY_USE_COLORS, false);
		AuthorizationService authService = new AuthorizationService(requiredDomain, dummyAuthorization);
		DesktopResourceService drs = new DesktopResourceService(authService, desktopUrl, allowInsecureSsl);
		IApplicationManagerFactory appManager = new JavaFxApplicationManagerFactory(
				new JavaFxKeyboard(new XpraKeyMap()));
		VirtueService virtueService = new VirtueService(drs, appManager);
		Sidebar sidebar = new Sidebar(virtueService, authService, useColors);
		SidebarController controller = new SidebarController(virtueService, sidebar, authService);
		controller.init(primaryStage);
	}
}
