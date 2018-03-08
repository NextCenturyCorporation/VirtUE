package com.ncc.savior.virtueadmin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
