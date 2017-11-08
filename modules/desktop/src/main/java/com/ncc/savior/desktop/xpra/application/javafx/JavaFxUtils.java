package com.ncc.savior.desktop.xpra.application.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Utility methods for extracting information from JavaFX events
 *
 *
 */
public class JavaFxUtils {
	public static final String MOD_ALT_STRING = "alt";
	public static final String MOD_CONTROL_STRING = "control";
	public static final String MOD_SHIFT_STRING = "shift";
	public static final String MOD_META_STRING = "meta";
	public static final String MOD_SHORTCUT_STRING = "shortcut";
	public static final String MOD_CAPS_LOCK = "lock";
	public static final String MOD_NUM_LOCK = "mod2";

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
		if (event.isShortcutDown()) {
			mods.add(MOD_SHORTCUT_STRING);
		}
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
		if (event.isShortcutDown()) {
			mods.add(MOD_SHORTCUT_STRING);
		}
		addLockKeys(mods);
		return mods;
	}

	private static void addLockKeys(List<String> mods) {
		boolean capslock = false;
		// Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
		boolean numlock = false;
		// Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_NUM_LOCK);
		// System.out.println(capslock + " : " + numlock);
		numlock = true;
		if (capslock) {
			mods.add(MOD_CAPS_LOCK);
		}
		if (numlock) {
			mods.add(MOD_NUM_LOCK);
		}

	}
}
