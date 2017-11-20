package com.ncc.savior.desktop.sidebar;

import com.ncc.savior.desktop.virtues.VirtueService;

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
		Sidebar sidebar = new Sidebar(new VirtueService());
		sidebar.start(primaryStage);

	}

}
