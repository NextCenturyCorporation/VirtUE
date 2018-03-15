package com.ncc.savior.virtueadmin.util;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions that are related to java itself and not a specific
 * framework.
 */
public class JavaUtil {
	private static final Logger logger = LoggerFactory.getLogger(JavaUtil.class);

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

}
