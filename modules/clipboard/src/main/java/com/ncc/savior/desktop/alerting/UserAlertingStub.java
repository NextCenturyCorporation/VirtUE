package com.ncc.savior.desktop.alerting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a placeholder for an alerting system. For now it will just help
 * us remember where we want to do user alerts. This implementation will just
 * print to the local logger, but will eventually be replaced with an entire
 * system.
 *
 *
 */
public class UserAlertingStub {
	private static final Logger logger = LoggerFactory.getLogger(UserAlertingStub.class);

	public static void sendStubAlert(String message) {
		new ToastMessage(message, 2000);
		logger.info("*ALERT*: " + message);
	}
}
