package com.ncc.savior.desktop.sidebar;

import com.ncc.savior.desktop.virtues.VirtueService;

import javafx.application.Application;
import javafx.stage.Stage;

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
