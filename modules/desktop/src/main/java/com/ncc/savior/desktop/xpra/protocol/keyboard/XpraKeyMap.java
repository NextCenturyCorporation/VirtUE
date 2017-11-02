package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.scene.input.KeyCode;

/**
 * Key map modeled after the Xpra javascript client.
 *
 * See https://www.xpra.org/trac/browser/xpra/trunk/src/html5/js/Keycodes.js
 *
 *
 */
public class XpraKeyMap implements IKeyMap {
	private final Map<Integer, KeyCodeDto> keycodesMap = new HashMap<>();

	public XpraKeyMap() {

		add(KeyCode.ESCAPE.ordinal(), -1, "Escape");
		add(KeyCode.TAB.ordinal(), -1, "Tab");
		add(KeyCode.CAPS.ordinal(), -1, "Caps_Lock");
		add(KeyCode.SHIFT.ordinal(), -1, "Shift_L");
		add(KeyCode.CONTROL.ordinal(), -1, "Control_L");
		add(KeyCode.META.ordinal(), -1, "Meta_L");
		add(KeyCode.ALT.ordinal(), -1, "Alt_L");
		add(KeyCode.SPACE.ordinal(), 32);
		add(KeyCode.ALT_GRAPH.ordinal(), -1, "Alt_R");
		// No meta right
		add(KeyCode.CONTEXT_MENU.ordinal(), -1, "Menu_R");
		// No control right
		// no shift right
		add(KeyCode.ENTER.ordinal(), 13, "Return");
		add(KeyCode.BACK_SPACE.ordinal(), -1, "BackSpace");
		// special keys:
		add(KeyCode.SCROLL_LOCK.ordinal(), -1, "Scroll_Lock");
		add(KeyCode.PAUSE.ordinal(), -1, "Pause");
		add(KeyCode.NUM_LOCK.ordinal(), -1, "Num_Lock");
		// Arrow pad:
		add(KeyCode.INSERT.ordinal(), -1, "Insert");
		add(KeyCode.HOME.ordinal(), -1, "Home");
		add(KeyCode.PAGE_UP.ordinal(), -1, "Page_Up");
		add(KeyCode.DELETE.ordinal(), -1, "Delete");
		add(KeyCode.END.ordinal(), -1, "End");
		add(KeyCode.PAGE_DOWN.ordinal(), -1, "Page_Down");

		// NUMPAD:
		add(KeyCode.DIVIDE.ordinal(), -1, "KP_Divide");
		add(KeyCode.MULTIPLY.ordinal(), -1, "KP_Multiply");
		add(KeyCode.SUBTRACT.ordinal(), -1, "KP_Subtract");// XPRA code has KP_Substract. Assuming that is a typo.
		add(KeyCode.ADD.ordinal(), -1, "KP_Add");
		// no keypad enter
		add(KeyCode.DECIMAL.ordinal(), -1, "KP_Decimal");
		// Num_lock off:
		// no keypad insert
		// no keypad end
		// no keypad down
		// no keypad next/page down
		// no keypad left
		add(KeyCode.CLEAR.ordinal(), -1, "KP_Begin");
		// no keypad right
		// no keypad home
		// no keypad up
		// no keypad prior/page up

		add(KeyCode.NUMPAD0.ordinal(), -1, "KP0"); // 48??
		add(KeyCode.NUMPAD1.ordinal(), -1, "KP1");
		add(KeyCode.NUMPAD2.ordinal(), -1, "KP2");
		add(KeyCode.NUMPAD3.ordinal(), -1, "KP3");
		add(KeyCode.NUMPAD4.ordinal(), -1, "KP4");
		add(KeyCode.NUMPAD5.ordinal(), -1, "KP5");
		add(KeyCode.NUMPAD6.ordinal(), -1, "KP6");
		add(KeyCode.NUMPAD7.ordinal(), -1, "KP7");
		add(KeyCode.NUMPAD8.ordinal(), -1, "KP8");
		add(KeyCode.NUMPAD9.ordinal(), -1, "KP9"); // 57??

		for (int i = 1; i <= 20; i++) {
			KeyCode code = KeyCode.valueOf("F" + i);
			add(code.ordinal(), -1, "F" + i);
		}

		add(KeyCode.SPACE.ordinal(), 0x0020, " ");
		add(KeyCode.EXCLAMATION_MARK.ordinal(), 0x0021, "!");
		add(KeyCode.QUOTEDBL.ordinal(), 0x0022);// , "\"");
		add(KeyCode.NUMBER_SIGN.ordinal(), 0x0023, "#");
		add(KeyCode.DOLLAR.ordinal(), 0x0024, "$");
		// No Percent!! add(KeyCode..ordinal(), 0x0025, " ");
		add(KeyCode.AMPERSAND.ordinal(), 0x0026, "&");
		add(KeyCode.QUOTE.ordinal(), 0x0027);// , "'");
		add(KeyCode.LEFT_PARENTHESIS.ordinal(), 0x0028, "(");
		add(KeyCode.RIGHT_PARENTHESIS.ordinal(), 0x0029, ")");
		add(KeyCode.ASTERISK.ordinal(), 0x002A, "*");
		add(KeyCode.PLUS.ordinal(), 0x002B);// , "+");
		add(KeyCode.COMMA.ordinal(), 0x002C);// , ",");
		add(KeyCode.MINUS.ordinal(), 0x002D);// , "-");
		add(KeyCode.PERIOD.ordinal(), 0x002E);// , ".");
		add(KeyCode.SLASH.ordinal(), 0x002F);// , "/");
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
		add(KeyCode.COLON.ordinal(), 0x003A);// , ":");
		add(KeyCode.SEMICOLON.ordinal(), 0x003B);// , ";");
		add(KeyCode.LESS.ordinal(), 0x003C);// , "<");
		add(KeyCode.EQUALS.ordinal(), 0x003D);// , "=");
		add(KeyCode.GREATER.ordinal(), 0x003E);// , ">");
		// No question mark!
		// add(KeyCode.QUESTION_MARK.ordinal(), 0x003F, "?");
		add(KeyCode.AT.ordinal(), 0x0040, "@");

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

		add(KeyCode.OPEN_BRACKET.ordinal(), 0x005B);// , "[");
		add(KeyCode.BACK_SLASH.ordinal(), 0x005C);// , "\\");
		add(KeyCode.CLOSE_BRACKET.ordinal(), 0x005D);// , "]");
		add(KeyCode.CIRCUMFLEX.ordinal(), 0x005E);// , "^");// asciicircum
		add(KeyCode.UNDERSCORE.ordinal(), 0x005F);// , "_");
		add(KeyCode.BACK_QUOTE.ordinal(), 0x0060);// , "`");// grave
		//Lower case letters seem to work as is
		add(KeyCode.BRACELEFT.ordinal(), 0x007B);// , "{");
		//add(KeyCode..ordinal(),"bar"  0x007C,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"braceright"  0x007D,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"asciitilde"  0x007E,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"nobreakspace"  0x00A0,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"exclamdown"  0x00A1,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"cent"  0x00A2,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"sterling"  0x00A3,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"currency"  0x00A4,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"yen"  0x00A5,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"brokenbar"  0x00A6,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"section"  0x00A7,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"diaeresis"  0x00A8,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"copyright"  0x00A9,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"ordfeminine"  0x00AA,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"guillemotleft"  0x00AB,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"notsign"  0x00AC,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"hyphen"  0x00AD,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"registered"  0x00AE,"");
//		add(KeyCode.BACK_QUOTE.ordinal(),"macron"  0x00AF,"");


		add(KeyCode.UP.ordinal(), -1, "Up");
		add(KeyCode.DOWN.ordinal(), -1, "Down");
		add(KeyCode.LEFT.ordinal(), -1, "Left");
		add(KeyCode.RIGHT.ordinal(), -1, "Right");

		add(KeyCode.BACK_SLASH.ordinal(), 0x007C);// , "|");
		add(KeyCode.A.ordinal(), 65, "A");
		add(KeyCode.B.ordinal(), 66, "b");
		add(KeyCode.C.ordinal(), 99, "C");
		add(KeyCode.D.ordinal(), 100);// , "C");
		add(KeyCode.E.ordinal(), 69);// , "C");
		add(KeyCode.F.ordinal(), -1, "f");// , "C");
		add(KeyCode.G.ordinal(), -1, "G");// , "C");

	}

	protected void add(int ordinal, int code) {
		add(ordinal, code, "U" + Integer.toHexString(code));
	}

	protected void add(int ordinal, int code, String name) {
		KeyCodeDto c = new KeyCodeDto();
		c.setKeyCode(code);
		c.setKeyName(name);
		c.setOrdinal(ordinal);
		keycodesMap.put(ordinal, c);
	}

	// private static void map(int keycode, char c) {
	// //keycode = (int)c;
	// keycodesMap.put(keycode, String.format("U%s",
	// Integer.toHexString(c).toUpperCase()));
	// System.out.println(keycode + " : " + keycodesMap.get(keycode));
	// }
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
	public KeyCodeDto getKeyCodeDto(int key) {
		KeyCodeDto dto = keycodesMap.get(key);
		return dto;
	}
}
