package com.ncc.savior.desktop.alerting;

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

	public static void sendAlert(BaseAlertMessage alertMessage) {
		if (alertService == null) {
			alertService = new LoggingAlertService();
		}
		alertService.displayAlert(alertMessage);
	}

	public static void setAlertService(IUserAlertService alertService) {
		UserAlertingServiceHolder.alertService = alertService;
	}
}
