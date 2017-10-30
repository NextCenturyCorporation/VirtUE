package com.ncc.savior.desktop.xpra.application.javafx;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Test main method.
 *
 *
 */
public class JavaFxTestRunner extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		XpraClient client = new XpraClient();

		// Debug Packet Handler is very useful for debugging but incurs a significant
		// performance penalty.
		// File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
		// DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
		// client.addPacketListener(debugHandler);
		// client.addPacketSendListener(debugHandler);

		JavaFxApplicationManager applicationManager = new JavaFxApplicationManager(client, primaryStage);

		// TODO client.addPacketListener(new
		// JavaFxXpraPacketHandler(primaryStage.getScene()));
		applicationManager.setDebugOutput(true);
		client.connect(new TcpConnectionFactory(),
				new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000));
	}

}
