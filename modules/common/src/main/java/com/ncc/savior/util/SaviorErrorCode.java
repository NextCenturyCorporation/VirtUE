package com.ncc.savior.util;

/**
 * Error codes for savior server. These are generally grouped into sections. See
 * comments below.
 * 
 *
 */
public enum SaviorErrorCode {
	//@formatter:off
	//User/authentication errors
	USER_DISABLED(2, "User has been disabled"), 
	USER_NOT_AUTHORIZED(3, "User is not authorized"), 
	REQUESTED_USER_NOT_LOGGED_IN(4, "User not logged in"),
	//Missing item database errors
	VIRTUE_TEMPLATE_ID_NOT_FOUND(20, "Virtue Tempalte was not found"), 
	VIRTUE_ID_NOT_FOUND(21, "Virtue ID was not found"), 
	VM_TEMPLATE_NOT_FOUND(22, "Virtual Machine template not found"), 
	VM_NOT_FOUND(23, "Virtual Machine not found"), 
	USER_NOT_FOUND(24, "User not found"), 
	APPLICATION_ID_NOT_FOUND(25, "Application not found"),
	//Configuration errors
	CONFIGURATION_ERROR(100, "Configuration error"),
	//Configuration specific to import
	IMPORT_NOT_FOUND(120, "Import was not found"),
	//Runtime like errors
	SERVICE_TIMEOUT(200, "Service timed out"),
	XPRA_FAILED(201, "Error with XPRA server"),
	//Other
	NOT_IMPLEMENTED(254, "function not implemented"), 
	UNKNOWN_ERROR(255, "unknown error");
	//@formatter:on
	private final int errorCode;
	private final String readableString;

	SaviorErrorCode(int errorCode, String readableString) {
		this.errorCode = errorCode;
		this.readableString = readableString;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getReadableString() {
		return readableString;
	}

	public int getValue() {
		return errorCode;
	}
}