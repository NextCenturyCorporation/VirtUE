package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.javafx.JavaFxUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * JavaFX implementation of {@link IKeyboard}
 *
 *
 */
public class SwingKeyboard implements IKeyboard {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(JavaFxKeyboard.class);
	private IKeyMap keymap;

	public SwingKeyboard(IKeyMap keymap) {
		logger.warn("Swing keyboard not implemented");
		this.keymap = keymap;
	}

	@Override
	public IKeyMap getKeyMap() {
		return keymap;
	}

	public List<String> getModifiers(KeyEvent event) {
		List<String> mods = JavaFxUtils.getModifiers(event);
		return mods;
	}

	public KeyCodeDto getKeyCodeFromEvent(KeyEvent event) {
		KeyCode code = event.getCode();
		int ordinal = code.ordinal();
		return keymap.getKeyCodeDto(ordinal);
	}
}
