package com.ncc.savior.desktop.jna;

import com.ncc.savior.desktop.clipboard.windows.IWindowsClipboardUser32;

public class WindowsJnaLockingKeyService implements ILockingKeysService {

	private IWindowsClipboardUser32 user32;

	public WindowsJnaLockingKeyService() {
		user32 = IWindowsClipboardUser32.INSTANCE;
	}

	@Override
	public boolean getLockingKeyState(int vKey) {
		short state = user32.GetKeyState(vKey);
		boolean toggled = (state & 0x01) == 1;
		if (state != 0 && state != 1) {
			System.out.println(Integer.toBinaryString(state));
		}
		return toggled;
	}

}
