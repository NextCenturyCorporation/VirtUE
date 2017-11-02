package com.ncc.savior.desktop.xpra.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.keyboard.JavaFxKeyboard;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
import com.ncc.savior.desktop.xpra.protocol.keyboard.XpraKeyMap;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class KeyboardDebugger extends Application {
	private static final Logger logger = LoggerFactory.getLogger(KeyboardDebugger.class);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("TextArea Experiment 1");

		Canvas canvas = new Canvas();

		VBox vbox = new VBox(canvas);

		Scene scene = new Scene(vbox, 200, 100);
		JavaFxKeyboard keyboard = new JavaFxKeyboard(new XpraKeyMap());

		scene.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
				logger.debug("Typed KeyCode=" + keycode);
			}
		});

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getText().length() == 0) {
					KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
					logger.debug("Pressed KeyCode=" + keycode);
				}
				// int key = event.getCode().ordinal();
				// String u = keyMap.getUnicodeName(key);
				// int c = keyMap.getKeyCode(key);
				// List<String> mods = JavaFxUtils.getModifiers(event);
				// if (keycode != null) {
				// onKeyDown(keycode, keyboard.getModifiers());
				// }
			}
		});

		scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				// KeyCodeDto keycode = keyboard.getKeyCodeFromEvent(event);
				// logger.debug("Released KeyCode=" + keycode);
				// int key = event.getCode().ordinal();
				// String u = keyMap.getUnicodeName(key);
				// int c = keyMap.getKeyCode(key);
				// List<String> mods = JavaFxUtils.getModifiers(event);
				// if (keycode != null) {
				// onKeyUp(keycode, keyboard.getModifiers());
				// }
			}
		});
		primaryStage.setScene(scene);
		primaryStage.show();

	}

}
