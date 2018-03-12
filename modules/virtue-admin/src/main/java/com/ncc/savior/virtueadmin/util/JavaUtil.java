package com.ncc.savior.virtueadmin.util;

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

}
