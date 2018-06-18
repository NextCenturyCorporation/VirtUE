package com.ncc.savior.desktop.jna;

import java.awt.event.KeyEvent;

import com.ncc.savior.util.JavaUtil;

/**
 * Test application to allow users to test and see if their system supports the
 * locking keys.
 *
 */
public class LockingKeysTestApp {
	public static void main(String[] args) {
		ILockingKeysService lks = getLockingKeyService(args);
		System.out.println("Java Locking Key Tester with " + lks.getClass().getSimpleName());
		System.out.print(
				"  To test the locking keys for your system, be sure to press the locking keys while this application does NOT have focus and see if the reported status stays accurate");
		System.out.println();
		System.out.println("**Starting locking key poll**");
		System.out.println();
		while (true) {
			boolean cl = lks.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
			boolean nl = lks.getLockingKeyState(KeyEvent.VK_NUM_LOCK);
			boolean sl = lks.getLockingKeyState(KeyEvent.VK_SCROLL_LOCK);

			System.out.println("Caps Lock: " + cl + "  Num Lock: " + nl + "  Scroll Lock: " + sl);
			JavaUtil.sleepAndLogInterruption(500);
		}
	}

	private static ILockingKeysService getLockingKeyService(String[] args) {
		if (args.length == 0) {
			return ILockingKeysService.getLockingKeyService();
		} else if (args[0].equalsIgnoreCase("native")) {
			return ILockingKeysService.getLockingKeyService();
		} else if (args[0].equalsIgnoreCase("java")) {
			return JavaLockingKeyService.getInstance();
		}
		usage();
		System.exit(-1);
		return null;
	}

	private static void usage() {
		// TODO Auto-generated method stub

	}
}
