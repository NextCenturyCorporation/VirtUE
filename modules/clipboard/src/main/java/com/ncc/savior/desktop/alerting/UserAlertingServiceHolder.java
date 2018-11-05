package com.ncc.savior.desktop.alerting;

import java.io.IOException;

import org.slf4j.Logger;

/**
 * This class is a placeholder for an alerting system. For now it will just help
 * us remember where we want to do user alerts. This implementation will just
 * print to the local logger, but will eventually be replaced with an entire
 * system.
 *
 *
 */

// TODO worry about synchronization??
public class UserAlertingServiceHolder {
	// private static final Logger logger =
	// LoggerFactory.getLogger(UserAlertingServiceHolder.class);
	private static IUserAlertService alertService;
	private static IAlertHistoryManager alertHistoryManager;

	public static void sendAlert(BaseAlertMessage alertMessage) throws IOException {
		if (alertService == null) {
			alertService = new LoggingAlertService();
		}

		if (alertHistoryManager == null) {
			alertHistoryManager = new AlertHistoryWriter();
		}

		alertService.displayAlert(alertMessage);

		alertHistoryManager.storeAlert(alertMessage);
	}

	public static void sendAlertLogError(BaseAlertMessage alertMessage, Logger logger) {
		try {
			sendAlert(alertMessage);
		} catch (IOException e) {
			logger.error("Failed to send alert message: " + alertMessage);
		}
	}

	public static void setAlertService(IUserAlertService alertService) {
		UserAlertingServiceHolder.alertService = alertService;
	}

	public static void resetHistoryManager() throws IOException {
		alertHistoryManager = new AlertHistoryWriter();
	}
}
