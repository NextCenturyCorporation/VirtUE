package com.ncc.savior.desktop.jna;

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;

/**
 * Windows JNA based {@link ILockingKeysService} implementation. Uses user32.dll
 * GetKeyState function.
 *
 *
 */
public class WindowsJnaLockingKeyService implements ILockingKeysService {

	// documentation says least significant bit. Most significant bit is whether it
	// is currently pressed
	private static final int LOCKING_KEY_TOGGLE_BIT = 0x01;
	private static WindowsJnaLockingKeyService INSTANCE = null;
	private static Object lock = new Object();
	private IWindowsClipboardUser32 user32;

	private WindowsJnaLockingKeyService() {
		user32 = IWindowsClipboardUser32.INSTANCE;
	}

	public static ILockingKeysService getInstance() {
		if (INSTANCE == null) {
			synchronized (lock) {
				if (INSTANCE == null) {
					INSTANCE = new WindowsJnaLockingKeyService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public boolean getLockingKeyState(int vKey) {
		short state = user32.GetKeyState(vKey);
		boolean toggled = (state & LOCKING_KEY_TOGGLE_BIT) == 1;
		return toggled;
	}
}
