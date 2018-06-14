package com.ncc.savior.desktop.jna;

import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.OS;

public interface ILockingKeysService {

	boolean getLockingKeyState(int vkCapsLock);

	static ILockingKeysService getLockingKeyService() {
		OS os = JavaUtil.getOs();
		switch (os) {
		case WINDOWS:
			// return new WindowsJnaLockingKeyService();
			return new JavaLockingKeyService();
		case LINUX:
			return new JavaLockingKeyService();
		case MAC:
			return new JavaLockingKeyService();
		default:
			return new JavaLockingKeyService();
		}
	}
}
