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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.application.swing.SwingUtils;

/**
 * Swing implementation of {@link IKeyboard}
 *
 *
 */
public class SwingKeyboard implements IKeyboard {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SwingKeyboard.class);
	private IKeyMap keymap;

	public SwingKeyboard(IKeyMap keymap) {
		this.keymap = keymap;
	}

	@Override
	public IKeyMap getKeyMap() {
		return keymap;
	}

	public List<String> getModifiers(KeyEvent event) {
		List<String> mods = SwingUtils.getModifiers(event);
		return mods;
	}

	public KeyCodeDto getKeyCodeFromEvent(KeyEvent event) {
		int code = event.getKeyCode();
		// char c = event.getKeyChar();
		// if (Character.isAlphabetic(c) || Character.isDigit(c) || c ==
		// KeyEvent.CHAR_UNDEFINED) {
		// char c = event.getKeyChar();
		KeyCodeDto keycode = keymap.getKeyCodeDto(code);
		// if (c != KeyEvent.CHAR_UNDEFINED) {
		// keycode.setStr(Character.toString(c));
		// }
		return keycode;
		// } else {
		// KeyCodeDto keycode = new KeyCodeDto();
		// keycode.setStr(Character.toString(event.getKeyChar()));
		// keycode.setKeyCode(event.getExtendedKeyCode());
		// keycode.setKeyVal(0);
		// String name = null;
		// name = "U" + (int)c;
		// keycode.setKeyName(name);
		// return keycode;
		// }
	}
}
