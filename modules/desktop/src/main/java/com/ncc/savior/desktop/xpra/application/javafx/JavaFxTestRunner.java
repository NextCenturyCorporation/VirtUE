package com.ncc.savior.desktop.xpra.application.javafx;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.connection.IConnectListener;
import com.ncc.savior.desktop.xpra.connection.IConnection;
import com.ncc.savior.desktop.xpra.connection.IConnectionParameters;
import com.ncc.savior.desktop.xpra.connection.tcp.TcpConnectionFactory;
import com.ncc.savior.desktop.xpra.debug.DebugPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Test main method.
 *
 *
 */
public class JavaFxTestRunner extends Application {
	private static final Logger logger = LoggerFactory.getLogger(JavaFxTestRunner.class);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		XpraClient client = new XpraClient();

		boolean useDebugHandler = false;
		// Debug Packet Handler is very useful for debugging but incurs a significant
		// performance penalty.
		useDebugHandler = true;
		if (useDebugHandler) {
			DebugPacketHandler.clearDefaultDebugFolder();
			File dir = DebugPacketHandler.getDefaultTimeBasedDirectory();
			DebugPacketHandler debugHandler = new DebugPacketHandler(dir);
			client.addPacketListener(debugHandler);
			client.addPacketSendListener(debugHandler);
		}
		JavaFxKeyboard keyboard = new JavaFxKeyboard(new XpraKeyMap());
		JavaFxApplicationManager applicationManager = new JavaFxApplicationManager(client, null, keyboard);

		applicationManager.setDebugOutput(true);
		client.addConnectListener(new IConnectListener() {
			@Override
			public void onConnectSuccess(IConnection connection) {
				logger.info("Connection Success");
			}

			@Override
			public void onConnectFailure(IConnectionParameters params, IOException e) {
				logger.info("Connection Failure");
			}

			@Override
			public void onBeforeConnectAttempt(IConnectionParameters parameters) {
				logger.info("Attempting to connect...");
			}
		});
		client.connect(new TcpConnectionFactory(),
				new TcpConnectionFactory.TcpConnectionParameters("localhost", 10000));
	}

}
