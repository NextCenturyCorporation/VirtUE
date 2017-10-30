package com.ncc.savior.desktop.xpra.application.javafx;

import java.util.ArrayList;
import java.util.List;

import com.ncc.savior.desktop.xpra.application.XpraWindowManager;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Utility methods for extracting information from JavaFX events
 *
 *
 */
public class JavaFxUtils {
	public static List<String> getModifiers(KeyEvent event) {
		List<String> mods = new ArrayList<String>();
		if (event.isAltDown()) {
			mods.add(XpraWindowManager.MOD_ALT_STRING);
		}
		if (event.isControlDown()) {
			mods.add(XpraWindowManager.MOD_CONTROL_STRING);
		}
		if (event.isShiftDown()) {
			mods.add(XpraWindowManager.MOD_SHIFT_STRING);
		}
		if (event.isMetaDown()) {
			mods.add(XpraWindowManager.MOD_META_STRING);
		}
		if (event.isShortcutDown()) {
			mods.add(XpraWindowManager.MOD_SHORTCUT_STRING);
		}
		return mods;
	}

	public static List<String> getModifiers(MouseEvent event) {
		List<String> mods = new ArrayList<String>();
		if (event.isAltDown()) {
			mods.add(XpraWindowManager.MOD_ALT_STRING);
		}
		if (event.isControlDown()) {
			mods.add(XpraWindowManager.MOD_CONTROL_STRING);
		}
		if (event.isShiftDown()) {
			mods.add(XpraWindowManager.MOD_SHIFT_STRING);
		}
		if (event.isMetaDown()) {
			mods.add(XpraWindowManager.MOD_META_STRING);
		}
		if (event.isShortcutDown()) {
			mods.add(XpraWindowManager.MOD_SHORTCUT_STRING);
		}
		return mods;
	}
}
