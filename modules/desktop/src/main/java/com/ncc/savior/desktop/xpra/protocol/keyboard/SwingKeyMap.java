/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.protocol.keyboard;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SwingKeyMap implements IKeyMap {
	private final Map<Integer, KeyCodeDto> keycodesMap = new HashMap<>();

	public SwingKeyMap() {
		add(KeyEvent.VK_ESCAPE, -1, "Escape");
		add(KeyEvent.VK_TAB, -1, "Tab");
		add(KeyEvent.VK_CAPS_LOCK, -1, "Caps_Lock");
		add(KeyEvent.VK_SHIFT, -1, "Shift_L");
		add(KeyEvent.VK_CONTROL, -1, "Control_L");
		add(KeyEvent.VK_META, -1, "Meta_L");
		add(KeyEvent.VK_ALT, -1, "Alt_L");
		add(KeyEvent.VK_SPACE, 32, 32, "space", " ");
		add(KeyEvent.VK_ALT_GRAPH, -1, "Alt_R");
		// No meta right
		add(KeyEvent.VK_CONTEXT_MENU, -1, "Menu_R");
		// No control right
		// no shift right
		add(KeyEvent.VK_ENTER, 13, "Return");
		add(KeyEvent.VK_BACK_SPACE, -1, "BackSpace");
		// special keys:
		add(KeyEvent.VK_SCROLL_LOCK, -1, "Scroll_Lock");
		add(KeyEvent.VK_PAUSE, -1, "Pause");
		add(KeyEvent.VK_NUM_LOCK, -1, "Num_Lock");
		// Arrow pad:
		add(KeyEvent.VK_INSERT, -1, "Insert");
		add(KeyEvent.VK_HOME, -1, "Home");
		add(KeyEvent.VK_PAGE_UP, -1, "Page_Up");
		add(KeyEvent.VK_DELETE, -1, "Delete");
		add(KeyEvent.VK_END, -1, "End");
		add(KeyEvent.VK_PAGE_DOWN, -1, "Page_Down");

		// NUMPAD:
		add(KeyEvent.VK_DIVIDE, -1, "KP_Divide");
		add(KeyEvent.VK_MULTIPLY, -1, "KP_Multiply");
		add(KeyEvent.VK_SUBTRACT, -1, "KP_Subtract");// XPRA code has KP_Substract. Assuming that is a typo.
		add(KeyEvent.VK_ADD, -1, "KP_Add");
		// no keypad enter
		add(KeyEvent.VK_DECIMAL, -1, "KP_Decimal");
		// Num_lock off:
		// no keypad insert
		// no keypad end
		// no keypad down
		// no keypad next/page down
		// no keypad left
		add(KeyEvent.VK_CLEAR, -1, "KP_Begin");
		// no keypad right
		// no keypad home
		// no keypad up
		// no keypad prior/page up

		add(KeyEvent.VK_NUMPAD0, -1, "KP_0"); // 48??
		add(KeyEvent.VK_NUMPAD1, -1, "KP_1");
		add(KeyEvent.VK_NUMPAD2, -1, "KP_2");
		add(KeyEvent.VK_NUMPAD3, -1, "KP_3");
		add(KeyEvent.VK_NUMPAD4, -1, "KP_4");
		add(KeyEvent.VK_NUMPAD5, -1, "KP_5");
		add(KeyEvent.VK_NUMPAD6, -1, "KP_6");
		add(KeyEvent.VK_NUMPAD7, -1, "KP_7");
		add(KeyEvent.VK_NUMPAD8, -1, "KP_8");
		add(KeyEvent.VK_NUMPAD9, -1, "KP_9"); // 57??

		add(KeyEvent.VK_F1, -1, "F1");
		add(KeyEvent.VK_F2, -1, "F2");
		add(KeyEvent.VK_F3, -1, "F3");
		add(KeyEvent.VK_F4, -1, "F4");
		add(KeyEvent.VK_F5, -1, "F5");
		add(KeyEvent.VK_F6, -1, "F6");
		add(KeyEvent.VK_F7, -1, "F7");
		add(KeyEvent.VK_F8, -1, "F8");
		add(KeyEvent.VK_F9, -1, "F9");
		add(KeyEvent.VK_F10, -1, "F10");
		add(KeyEvent.VK_F11, -1, "F11");
		add(KeyEvent.VK_F12, -1, "F12");
		add(KeyEvent.VK_F13, -1, "F13");
		add(KeyEvent.VK_F14, -1, "F14");
		add(KeyEvent.VK_F15, -1, "F15");
		add(KeyEvent.VK_F16, -1, "F16");
		add(KeyEvent.VK_F17, -1, "F17");
		add(KeyEvent.VK_F18, -1, "F18");
		add(KeyEvent.VK_F19, -1, "F19");
		add(KeyEvent.VK_F20, -1, "F20");

		add(KeyEvent.VK_QUOTE, 39, 222, "apostrophe", "'");// , "'");

		add(KeyEvent.VK_COMMA, 44, 188, "comma", ",");// , ",");
		add(KeyEvent.VK_MINUS, 45, 189, "minus", "-");// , "-");
		add(KeyEvent.VK_PERIOD, 46, 190, "period", ".");// , ".");
		add(KeyEvent.VK_SLASH, 47, 191, "slash", "/");// , "/");
		add(KeyEvent.VK_0, 48, "0");
		add(KeyEvent.VK_1, 49, "1");
		add(KeyEvent.VK_2, 50, "2");
		add(KeyEvent.VK_3, 51, "3");
		add(KeyEvent.VK_4, 52, "4");
		add(KeyEvent.VK_5, 53, "5");
		add(KeyEvent.VK_6, 54, "6");
		add(KeyEvent.VK_7, 55, "7");
		add(KeyEvent.VK_8, 56, "8");
		add(KeyEvent.VK_9, 57, "9");

		add(KeyEvent.VK_A, 65, "A");
		add(KeyEvent.VK_B, 66, "B");
		add(KeyEvent.VK_C, 67, "C");
		add(KeyEvent.VK_D, 68, "D");
		add(KeyEvent.VK_E, 69, "E");
		add(KeyEvent.VK_F, 70, "F");
		add(KeyEvent.VK_G, 71, "G");
		add(KeyEvent.VK_H, 72, "H");
		add(KeyEvent.VK_I, 73, "I");
		add(KeyEvent.VK_J, 74, "J");
		add(KeyEvent.VK_K, 75, "K");
		add(KeyEvent.VK_L, 76, "L");
		add(KeyEvent.VK_M, 77, "M");
		add(KeyEvent.VK_N, 78, "N");
		add(KeyEvent.VK_O, 79, "O");
		add(KeyEvent.VK_P, 80, "P");
		add(KeyEvent.VK_Q, 81, "Q");
		add(KeyEvent.VK_R, 82, "R");
		add(KeyEvent.VK_S, 83, "S");
		add(KeyEvent.VK_T, 84, "T");
		add(KeyEvent.VK_U, 85, "U");
		add(KeyEvent.VK_V, 86, "V");
		add(KeyEvent.VK_W, 87, "W");
		add(KeyEvent.VK_X, 88, "X");
		add(KeyEvent.VK_Y, 89, "Y");
		add(KeyEvent.VK_Z, 90, "Z");
		// Lower case letters seem to work as is

		add(KeyEvent.VK_OPEN_BRACKET, 91, 219, "bracketleft", "[");// , "[");
		add(KeyEvent.VK_BACK_SLASH, 92, 220, "backslash", "\\");
		add(KeyEvent.VK_CLOSE_BRACKET, 93, 221, "bracketright", "]");// , "]");
		add(KeyEvent.VK_BACK_QUOTE, 96, 192, "grave", "`");// , "`");// grave

		add(KeyEvent.VK_CIRCUMFLEX, 0x005E);// , "^");// asciicircum
		add(KeyEvent.VK_UNDERSCORE, 0x005F);// , "_");
		add(KeyEvent.VK_BRACELEFT, 0x007B);// , "{");
		add(KeyEvent.VK_SEMICOLON, 59, 186, "semicolon", ";");// , ";");
		add(KeyEvent.VK_EQUALS, 61, 187, "equal", "=");// , "=");
		add(KeyEvent.VK_GREATER, 0x003E);// , ">");
		add(KeyEvent.VK_COLON, 0x003A);// , ":");
		add(KeyEvent.VK_LESS, 0x003C);// , "<");
		add(KeyEvent.VK_AT, 0x0040, "@");
		add(KeyEvent.VK_ASTERISK, 0x002A, "*");
		add(KeyEvent.VK_PLUS, 0x002B);// , "+");
		add(KeyEvent.VK_EXCLAMATION_MARK, 0x0021, "!");
		add(KeyEvent.VK_QUOTEDBL, 0x0022);// , "\"");
		add(KeyEvent.VK_NUMBER_SIGN, 0x0023, "#");
		add(KeyEvent.VK_DOLLAR, 0x0024, "$");
		add(KeyEvent.VK_AMPERSAND, 0x0026, "&");
		add(KeyEvent.VK_LEFT_PARENTHESIS, 0x0028, "(");
		add(KeyEvent.VK_RIGHT_PARENTHESIS, 0x0029, ")");
		// add(KeyEvent.VK_P, 0x0025, " ");
		// No question mark!
		// add(KeyEvent.VK_QUESTION_MARK, 0x003F, "?");

		add(KeyEvent.VK_UP, -1, "Up");
		add(KeyEvent.VK_DOWN, -1, "Down");
		add(KeyEvent.VK_LEFT, -1, "Left");
		add(KeyEvent.VK_RIGHT, -1, "Right");
	}

	private void add(int ordinal, int val, int code, String name, String str) {
		KeyCodeDto c = new KeyCodeDto();
		c.setKeyCode(code);
		c.setKeyName(name);
		c.setKeyVal(val);
		c.setStr(str);
		keycodesMap.put(ordinal, c);
	}

	protected void add(int ordinal, int val, int code) {
		add(ordinal, val, code, "U" + Integer.toHexString(code));

	}

	protected void add(int ordinal, int code) {
		add(ordinal, code, code, "U" + Integer.toHexString(code));
	}

	protected void add(int ordinal, int code, String name) {
		add(ordinal, code, code, name);
	}

	protected void add(int ordinal, int keyVal, int code, String name) {
		KeyCodeDto c = new KeyCodeDto();
		c.setKeyVal(keyVal);
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
