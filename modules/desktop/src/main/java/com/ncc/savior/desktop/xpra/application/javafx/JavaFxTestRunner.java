package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.File;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.debug.DebugPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.keyboard.BruteForceKeyMap;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class JavaFxTestRunner extends Application {

	public static void main(String[] args) {

		launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		BruteForceKeyMap keymap = new BruteForceKeyMap();
		XpraClient client = new XpraClient(keymap);
		File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
		DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
		client.addPacketListener(debugHandler);
		client.addPacketSendListener(debugHandler);

		Group root = new Group();
		AnchorPane anchor = new AnchorPane();
		root.getChildren().add(anchor);
		primaryStage.setScene(new Scene(root, 1200, 800));
		primaryStage.show();
		JavaFxXpraWindowManager manager = new JavaFxXpraWindowManager(client, primaryStage, anchor);
		client.addPacketListener(new JavaFxXpraPacketHandler(primaryStage.getScene()));
		manager.setDebugOutput(true);
		client.connect(new TcpConnectionFactory(),
				new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000));
	}

}
