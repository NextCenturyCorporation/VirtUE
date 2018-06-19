package com.ncc.savior.desktop.jna;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.OS;

/**
 * Service that provides the state of the locking keys.
 *
 *
 */
public interface ILockingKeysService {

	boolean getLockingKeyState(int vkCapsLock);

	static ILockingKeysService getLockingKeyService() {
		OS os = JavaUtil.getOs();
		switch (os) {
		case WINDOWS:
			return WindowsJnaLockingKeyService.getInstance();
		// return JavaLockingKeyService.getInstance();
		case LINUX:
			return JavaLockingKeyService.getInstance();
		case MAC:
			return JavaLockingKeyService.getInstance();
		default:
			return JavaLockingKeyService.getInstance();
		}
	}
}
