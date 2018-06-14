package com.ncc.savior.desktop.jna;

import java.awt.Toolkit;

/**
 * Java implementation of {@link ILockingKeysService}. Note that this
 * implementation is no reliable on all systems! Particularly Windows 7.
 *
 *
 */
public class JavaLockingKeyService implements ILockingKeysService {

	@Override
	public boolean getLockingKeyState(int key) {
		return Toolkit.getDefaultToolkit().getLockingKeyState(key);
	}



}
