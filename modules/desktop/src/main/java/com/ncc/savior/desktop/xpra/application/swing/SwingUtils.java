package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static KeyCodeDto getKeyCodeFromEvent(KeyEvent e, SwingKeyboard keyboard) {
		KeyCodeDto code = keyboard.getKeyCodeFromEvent(e);
		return code;
	}
}
