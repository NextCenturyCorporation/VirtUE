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
	USER_DISABLED(2, "User has been disabled", 403), 
	USER_NOT_AUTHORIZED(3, "User is not authorized", 403), 
	REQUESTED_USER_NOT_LOGGED_IN(4, "User not logged in"),
	//Missing item database errors
	//Should these have 404s?  it somewhat depends on where they are called
	VIRTUE_TEMPLATE_ID_NOT_FOUND(20, "Virtue Template was not found", 400), 
	VIRTUE_ID_NOT_FOUND(21, "Virtue ID was not found", 400), 
	VM_TEMPLATE_NOT_FOUND(22, "Virtual Machine template not found", 400), 
	VM_NOT_FOUND(23, "Virtual Machine not found", 400), 
	USER_NOT_FOUND(24, "User not found", 400), 
	APPLICATION_ID_NOT_FOUND(25, "Application not found", 400),
	ID_MISMATCH(26, "Object IDs do not match"),
	//bad data?
	INVALID_INPUT(50, "Invalid input"),
	//Configuration errors
	CONFIGURATION_ERROR(100, "Configuration error", 500),
	//Configuration specific to import
	IMPORT_NOT_FOUND(120, "Import was not found"),
	//Runtime like errors
	SERVICE_TIMEOUT(200, "Service timed out", 500),
	XPRA_FAILED(201, "Error with XPRA server", 500),
	AWS_ERROR(202, "Error with AWS", 500),
	JSON_ERROR(203, "Error reading json", 500),
	INVALID_STATE(204, "Object in invalid state to perform action", 500),
	//Other
	NOT_IMPLEMENTED(254, "function not implemented", 501), 
	UNKNOWN_ERROR(255, "unknown error"), 
	//Errors we shouldn't see because its older code
	VIRTUAL_BOX_ERROR(1001, "Error with virtual box") ;
	//@formatter:on
	private final int errorCode;
	private final String readableString;
	private int httpResponseCode;

	SaviorErrorCode(int errorCode, String readableString) {
		this(errorCode, readableString, 400);
	}

	SaviorErrorCode(int errorCode, String readableString, int httpResponseCode) {
		this.errorCode = errorCode;
		this.readableString = readableString;
		this.httpResponseCode = httpResponseCode;
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

	public int getHttpResponseCode() {
		return httpResponseCode;
	}
}
