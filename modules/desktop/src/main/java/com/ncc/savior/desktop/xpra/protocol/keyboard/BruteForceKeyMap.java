package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.scene.input.KeyCode;

/**
 * Test implementation of a key map. This was created by hand and is probably
 * not very flexible with different keyboard locals and formats.
 *
 *
 */
public class BruteForceKeyMap implements IKeyMap {

	private final Map<Integer, KeyCodeDto> keycodesMap = new HashMap<>();

	public BruteForceKeyMap() {
		add(KeyCode.DIGIT0.ordinal(), 48, "0");
		add(KeyCode.DIGIT1.ordinal(), 49, "1");
		add(KeyCode.DIGIT2.ordinal(), 50, "2");
		add(KeyCode.DIGIT3.ordinal(), 51, "3");
		add(KeyCode.DIGIT4.ordinal(), 52, "4");
		add(KeyCode.DIGIT5.ordinal(), 53, "5");
		add(KeyCode.DIGIT6.ordinal(), 54, "6");
		add(KeyCode.DIGIT7.ordinal(), 55, "7");
		add(KeyCode.DIGIT8.ordinal(), 56, "8");
		add(KeyCode.DIGIT9.ordinal(), 57, "9");

		add(KeyCode.A.ordinal(), 65, "A");
		add(KeyCode.B.ordinal(), 66, "B");
		add(KeyCode.C.ordinal(), 67, "C");
		add(KeyCode.D.ordinal(), 68, "D");
		add(KeyCode.E.ordinal(), 69, "E");
		add(KeyCode.F.ordinal(), 70, "F");
		add(KeyCode.G.ordinal(), 71, "G");
		add(KeyCode.H.ordinal(), 72, "H");
		add(KeyCode.I.ordinal(), 73, "I");
		add(KeyCode.J.ordinal(), 74, "J");
		add(KeyCode.K.ordinal(), 75, "K");
		add(KeyCode.L.ordinal(), 76, "L");
		add(KeyCode.M.ordinal(), 77, "M");
		add(KeyCode.N.ordinal(), 78, "N");
		add(KeyCode.O.ordinal(), 79, "O");
		add(KeyCode.P.ordinal(), 80, "P");
		add(KeyCode.Q.ordinal(), 81, "Q");
		add(KeyCode.R.ordinal(), 82, "R");
		add(KeyCode.S.ordinal(), 83, "S");
		add(KeyCode.T.ordinal(), 84, "T");
		add(KeyCode.U.ordinal(), 85, "U");
		add(KeyCode.V.ordinal(), 86, "V");
		add(KeyCode.W.ordinal(), 87, "W");
		add(KeyCode.X.ordinal(), 88, "X");
		add(KeyCode.Y.ordinal(), 89, "Y");
		add(KeyCode.Z.ordinal(), 90, "Z");

		add(KeyCode.SPACE.ordinal(), 32);
		add(KeyCode.ESCAPE.ordinal(), -1, "Escape");

		// right side special
		add(KeyCode.MINUS.ordinal(), 45);
		add(KeyCode.EQUALS.ordinal(), 61);
		add(KeyCode.BACK_SPACE.ordinal(), 8, "BackSpace");
		add(KeyCode.OPEN_BRACKET.ordinal(), 91);
		add(KeyCode.CLOSE_BRACKET.ordinal(), 93);
		add(KeyCode.BACK_SLASH.ordinal(), 92);
		add(KeyCode.SEMICOLON.ordinal(), 59);
		add(KeyCode.QUOTE.ordinal(), 39);
		add(KeyCode.ENTER.ordinal(), 13, "Return");
		add(KeyCode.COMMA.ordinal(), 44);
		add(KeyCode.PERIOD.ordinal(), 46);
		add(KeyCode.SLASH.ordinal(), 47);

		// Document navigation and arrow
		add(KeyCode.INSERT.ordinal(), -1, "Insert");
		add(KeyCode.DELETE.ordinal(), -1, "Delete");
		add(KeyCode.HOME.ordinal(), -1, "Home");
		add(KeyCode.END.ordinal(), -1, "End");
		add(KeyCode.PAGE_UP.ordinal(), -1, "Page_Up");
		add(KeyCode.PAGE_DOWN.ordinal(), -1, "Page_Down");

		add(KeyCode.UP.ordinal(), -1, "Up");
		add(KeyCode.DOWN.ordinal(), -1, "Down");
		add(KeyCode.LEFT.ordinal(), -1, "Left");
		add(KeyCode.RIGHT.ordinal(), -1, "Right");

		// Control
		add(KeyCode.TAB.ordinal(), -1, "Tab");// works
		add(KeyCode.ALT.ordinal(), -1, "Alt_L");
		add(KeyCode.ALT_GRAPH.ordinal(), -1, "Alt_R");
		// CAPS_LOCK
		// SHIFT
		// CTRL
		// WINDOWS KEY

		// Keypad

		// PrntScreen/ scrolllock, pause

		// Function keys

		// Needs testing
		add(KeyCode.F1.ordinal(), -1, "F1");
		add(KeyCode.F2.ordinal(), -1, "F2");
		add(KeyCode.F3.ordinal(), -1, "F3");
		add(KeyCode.F4.ordinal(), -1, "F4");
		add(KeyCode.F5.ordinal(), -1, "F5");
		add(KeyCode.F6.ordinal(), -1, "F6");
		add(KeyCode.F7.ordinal(), -1, "F7");
		add(KeyCode.F8.ordinal(), -1, "F8");
		add(KeyCode.F9.ordinal(), -1, "F9");
		add(KeyCode.F10.ordinal(), -1, "F10");
		add(KeyCode.F11.ordinal(), -1, "F11");
		add(KeyCode.F12.ordinal(), -1, "F12");

		add(KeyCode.QUOTEDBL.ordinal(), 34);

		add(KeyCode.LEFT_PARENTHESIS.ordinal(), 40);
		add(KeyCode.RIGHT_PARENTHESIS.ordinal(), 41);

		add(KeyCode.CAPS.ordinal(), -1, "Caps_Lock");
		add(KeyCode.SHIFT.ordinal(), -1, "Shift_L");
		add(KeyCode.CONTROL.ordinal(), -1, "Control_L");

		add(KeyCode.NUMPAD0.ordinal(), -1, "KP_0");
		add(KeyCode.NUMPAD1.ordinal(), -1, "KP_1");
		add(KeyCode.NUMPAD2.ordinal(), -1, "KP_2");
		add(KeyCode.NUMPAD3.ordinal(), -1, "KP_3");
		add(KeyCode.NUMPAD4.ordinal(), -1, "KP_4");
		add(KeyCode.NUMPAD5.ordinal(), -1, "KP_5");
		add(KeyCode.NUMPAD6.ordinal(), -1, "KP_6");
		add(KeyCode.NUMPAD7.ordinal(), -1, "KP_7");
		add(KeyCode.NUMPAD8.ordinal(), -1, "KP_8");
		add(KeyCode.NUMPAD9.ordinal(), -1, "KP_9");
	}

	protected void add(int ordinal, int code) {
		add(ordinal, code, "U" + Integer.toHexString(code));
	}

	protected void add(int ordinal, int code, String name) {
		KeyCodeDto c = new KeyCodeDto();
		c.setKeyCode(code);
		c.setKeyName(name);
		keycodesMap.put(ordinal, c);
	}

	@Override
	public String getUnicodeName(int keycode) {

		KeyCodeDto code = keycodesMap.get(keycode);
		if (code == null) {
			return null;
		}
		return code.getKeyName();
	}

	public Set<Entry<Integer, KeyCodeDto>> getEntries() {
		return keycodesMap.entrySet();
	}

	@Override
	public int getKeyCode(int key) {
		KeyCodeDto code = keycodesMap.get(key);
		if (code == null) {
			return -1;
		}
		return code.getKeyCode();
	}

	@Override
	public Collection<KeyCodeDto> getKeyCodes() {
		return keycodesMap.values();
	}

	@Override
	public KeyCodeDto getKeyCodeDto(int ordinal) {
		return keycodesMap.get(ordinal);
	}

}
