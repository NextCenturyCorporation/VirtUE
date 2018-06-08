package com.ncc.savior.util;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.virtueadmin.model.OS;

/**
 * Utility functions that are related to java itself and not a specific
 * framework.
 */
public class JavaUtil {
	private static final Logger logger = LoggerFactory.getLogger(JavaUtil.class);
	private static final String PROPERTY_OS_NAME = "os.name";
	private static final String OS_STRING_AIX = "aix";
	private static final String OS_STRING_LINUX = "nux";
	private static final String OS_STRING_UNIX = "nix";
	private static final String OS_STRING_WINDOWS = "win";
	private static final String OS_STRING_MAC = "mac";
	private static final String OS_STRING_SOLARIS = "sunos";

	public static void sleepAndLogInterruption(long periodMillis) {
		try {
			Thread.sleep(periodMillis);
		} catch (InterruptedException e) {
			logger.error("Unexpected sleep interruption!", e);
		}
	}

	public static boolean isNotEmpty(String stringToTest) {
		return stringToTest != null && !stringToTest.trim().equals("");
	}

	public static void closeIgnoreErrors(Closeable... closeables) {
		for (Closeable closeable : closeables) {
			try {
				closeable.close();
			} catch (Throwable t) {
				// do nothing
			}
		}
	}

	public static OS getOs() {
		String prop = System.getProperty(PROPERTY_OS_NAME);
		if (prop == null) {
			throw new RuntimeException("Unable to find OS from " + PROPERTY_OS_NAME);
		}
		prop = prop.toLowerCase();
		if (prop.indexOf(OS_STRING_WINDOWS) >= 0) {
			return OS.WINDOWS;
		} else if (prop.indexOf(OS_STRING_MAC) >= 0) {
			return OS.MAC;
		} else if (prop.indexOf(OS_STRING_SOLARIS) >= 0) {
			throw new RuntimeException("Solaris not supported!");
		} else if (prop.indexOf(OS_STRING_UNIX) >= 0 || prop.indexOf(OS_STRING_LINUX) >= 0
				|| prop.indexOf(OS_STRING_AIX) > 0) {
			return OS.LINUX;
		}
		throw new RuntimeException("Unsupported OS string=" + prop);
	}

	public static void startMemLogger(long periodMillis) {
		Runnable runnable = () -> {
			while (true) {
				Runtime rt = Runtime.getRuntime();
				logger.info("MemLogger: Total=" + getPrintBytes(rt.totalMemory()) + "Free="
						+ getPrintBytes(rt.freeMemory())
						+ "Max=" + getPrintBytes(rt.maxMemory()));
				JavaUtil.sleepAndLogInterruption(periodMillis);
			}
		};
		Thread t = new Thread(runnable, "MemLogger");
		t.setDaemon(true);
		t.start();
	}

	private static String[] byteDenomination = new String[] { "bytes", "KB", "MB", "GB", "TB", "?B" };
	private static String getPrintBytes(long memory) {
		double m = memory;
		int index = 0;
		while (m > 1024 * 5) {
			m = m / 1024;
			index++;
		}
		index = (index >= byteDenomination.length ? byteDenomination.length - 1 : index);
		String denom = byteDenomination[index];
		return String.format("%.3f %s", m, denom);
	}

}
