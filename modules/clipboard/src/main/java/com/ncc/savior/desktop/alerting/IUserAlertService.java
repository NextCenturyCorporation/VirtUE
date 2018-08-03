package com.ncc.savior.desktop.alerting;

/**
 * Service that handles display of user alert messages (
 * {@link BaseAlertMessage} ) as they occur.
 * 
 *
 */
public interface IUserAlertService {

	/**
	 * Called when an alert occurs and some system wants the service to display that
	 * alert.
	 * 
	 * @param alertMessage
	 */
	void displayAlert(BaseAlertMessage alertMessage);

}
