package com.ncc.savior.desktop.sidebar;

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
 * This is the main class to start just the sidebar application. This should be
 * used only for testing.
 *
 *
 */
public class SidebarApplication extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO use configuration for api
		// Plumbing and depedency injection
		AuthorizationService authService = new AuthorizationService();
		DesktopResourceService drs = new DesktopResourceService(authService, "http://localhost:8080/desktop/");
		IApplicationManagerFactory appManager = new JavaFxApplicationManagerFactory(
				new JavaFxKeyboard(new XpraKeyMap()));
		VirtueService virtueService = new VirtueService(drs, appManager);
		Sidebar sidebar = new Sidebar(virtueService, authService);
		SidebarController controller = new SidebarController(virtueService, sidebar);
		controller.init(primaryStage);
	}

}
