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
package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.alerting.BaseAlertMessage;
import com.ncc.savior.desktop.alerting.PlainAlertMessage;
import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.jna.ILockingKeysService;
import com.ncc.savior.desktop.xpra.protocol.keyboard.KeyCodeDto;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;

/**
 * Utility methods for extracting information from Swing events
 *
 *
 */
public class SwingUtils {
	private static final Logger logger = LoggerFactory.getLogger(SwingUtils.class);

	public static final String MOD_ALT_STRING = "alt";
	public static final String MOD_CONTROL_STRING = "control";
	public static final String MOD_SHIFT_STRING = "shift";
	public static final String MOD_META_STRING = "meta";
	public static final String MOD_SHORTCUT_STRING = "shortcut";
	public static final String MOD_CAPS_LOCK = "lock";
	public static final String MOD_NUM_LOCK = "mod2";
	private static ILockingKeysService lockingKeys;

	static {
		lockingKeys = ILockingKeysService.getLockingKeyService();
	}

	public static List<String> getModifiers(KeyEvent event) {
		List<String> mods = new ArrayList<String>();
		if (event.isAltDown()) {
			mods.add(MOD_ALT_STRING);
		}
		if (event.isControlDown()) {
			mods.add(MOD_CONTROL_STRING);
		}
		if (event.isShiftDown()) {
			mods.add(MOD_SHIFT_STRING);
		}
		if (event.isMetaDown()) {
			mods.add(MOD_META_STRING);
		}
		// if (event.isShortcutDown()) {
		// mods.add(MOD_SHORTCUT_STRING);
		// }
		addLockKeys(mods);
		return mods;
	}

	public static List<String> getModifiers(MouseEvent event) {
		List<String> mods = new ArrayList<String>();
		if (event.isAltDown()) {
			mods.add(MOD_ALT_STRING);
		}
		if (event.isControlDown()) {
			mods.add(MOD_CONTROL_STRING);
		}
		if (event.isShiftDown()) {
			mods.add(MOD_SHIFT_STRING);
		}
		if (event.isMetaDown()) {
			mods.add(MOD_META_STRING);
		}
		// if (event.isShortcutDown()) {
		// mods.add(MOD_SHORTCUT_STRING);
		// }
		addLockKeys(mods);
		return mods;
	}

	private static void addLockKeys(List<String> mods) {
		boolean capsLock = false, numLock = false;
		try {
			capsLock = lockingKeys.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
			numLock = lockingKeys.getLockingKeyState(KeyEvent.VK_NUM_LOCK);
			// boolean scrollLock = lockingKeys.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);
		} catch (Throwable t) {
			logger.error("Error getting locking key state", t);
		}
		numLock = true;
		if (capsLock) {
			mods.add(MOD_CAPS_LOCK);
		}
		if (numLock) {
			mods.add(MOD_NUM_LOCK);
		}
	}

	public static Rectangle fixOutOfBounds(int x, int y, int width, int height) {
		// make sure we slide for title bar and inset
		Rectangle window = new Rectangle(x, y, width, height);

		// this if is a fix for XPRA not supporting monitors in negative space. (or our
		// app not adjusting correctly)
		if (x + width > 0 && y + height > 0) {

			GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			for (GraphicsDevice screen : screens) {
				Rectangle bounds = screen.getDefaultConfiguration().getBounds();
				Rectangle union = bounds.intersection(window);
				// logger.debug("" + union);
				if (union != null && union.width > 0 && union.height > 0) {
					return window;
				}
			}
		} else {
			BaseAlertMessage alertMessage = new PlainAlertMessage("Window Moved",
					"A window has been moved because windows cannot be fully contained in a monitor that has negative coordinate space.  To Fix this issue, please set your top left monitor your primary monitor.");
			UserAlertingServiceHolder.sendAlertLogError(alertMessage, logger);
		}
		GraphicsDevice ds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle bounds = ds.getDefaultConfiguration().getBounds();
		// if we are out of bounds, lets just center the window in the center of the
		// default display.
		x = (int) (bounds.getCenterX() - width / 2);
		y = (int) (bounds.getCenterY() - height / 2);
		window.setLocation(x, y);
		return window;
	}

	public static KeyCodeDto getKeyCodeFromEvent(KeyEvent e, SwingKeyboard keyboard) {
		KeyCodeDto code = keyboard.getKeyCodeFromEvent(e);
		return code;
	}
}
