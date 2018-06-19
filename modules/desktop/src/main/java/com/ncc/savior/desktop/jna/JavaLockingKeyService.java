package com.ncc.savior.desktop.jna;

import java.awt.Toolkit;

/**
 * Java implementation of {@link ILockingKeysService}. Note that this
 * implementation is no reliable on all systems! Particularly Windows 7.
 *
 *
 */
public class JavaLockingKeyService implements ILockingKeysService {

	private static JavaLockingKeyService INSTANCE = null;
	private static Object lock = new Object();

	private JavaLockingKeyService() {
	}

	public static ILockingKeysService getInstance() {
		if (INSTANCE == null) {
			synchronized (lock) {
				if (INSTANCE == null) {
					INSTANCE = new JavaLockingKeyService();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public boolean getLockingKeyState(int key) {
		return Toolkit.getDefaultToolkit().getLockingKeyState(key);
	}

}
