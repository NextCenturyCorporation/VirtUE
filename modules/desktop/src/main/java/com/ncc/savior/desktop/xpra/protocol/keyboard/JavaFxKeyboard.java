package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.javafx.JavaFxUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination.ModifierValue;
import javafx.scene.input.KeyEvent;

public class JavaFxKeyboard implements IKeyboard {
	private static final Logger logger = LoggerFactory.getLogger(JavaFxKeyboard.class);
	private IKeyMap keymap;

	public JavaFxKeyboard(IKeyMap keymap) {
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
		String name = code.getName();
		boolean isArrow = code.isArrowKey();
		boolean isDigit = code.isDigitKey();
		boolean isFunc = code.isFunctionKey();
		boolean isKeyPad = code.isKeypadKey();
		boolean isModifier = code.isModifierKey();
		boolean isLetter = code.isLetterKey();
		boolean isNav = code.isNavigationKey();
		boolean isWhitespace = code.isWhitespaceKey();
		String adfkmlnw = "ADFK-MLNW ";
		adfkmlnw += (isArrow ? 1 : 0);
		adfkmlnw += (isDigit ? 1 : 0);
		adfkmlnw += (isFunc ? 1 : 0);
		adfkmlnw += (isKeyPad ? 1 : 0);
		adfkmlnw += "-";
		adfkmlnw += (isModifier ? 1 : 0);
		adfkmlnw += (isLetter ? 1 : 0);
		adfkmlnw += (isNav ? 1 : 0);
		adfkmlnw += (isWhitespace ? 1 : 0);

		String action = event.getEventType().toString();
		// logger.info(action + ": " + adfkmlnw + " text:'" + event.getText() + "'" + "
		// Char="
		// + event.getCharacter()
		// + " Name="
		// + name + " ordinal:"
		// + ordinal + " code=" + code);
		// if (isKeyPad) {
		//
		// }
		// code, shift, control, alt, meta, shortcut
		// logger.debug(
		// "Text=" + event.getText() + " name=" + event.getCode().name() + " name=" +
		// event.getCode().getName());

		if (!event.getCode().isModifierKey()) {
			KeyCodeCombination kcc = new KeyCodeCombination(event.getCode(), getVal(event.isShiftDown()),
					getVal(event.isControlDown()), getVal(event.isAltDown()), getVal(event.isMetaDown()),
					getVal(event.isShortcutDown()));
			// logger.debug(kcc.getDisplayText() + " : " + kcc.getName());
		}
		return keymap.getKeyCodeDto(ordinal, event.isShiftDown());
	}

	private ModifierValue getVal(boolean isDown) {
		return isDown ? ModifierValue.DOWN : ModifierValue.UP;
	}

}
