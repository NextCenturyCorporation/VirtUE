package com.ncc.savior.virtueadmin.util;

/**
 * Generic exception for savior that could be passed to user or APIs
 * 
 *
 */
public class SaviorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	// List of errors and assigned codes.
	// TODO codes need to be assigned (255 means unassigned/unknown) and probably
	// should be turned in to enums.
	public static final int UNKNOWN_ERROR = 255;
	public static final int INVALID_TEMPATE_ID = 255;
	public static final int VIRTUE_ID_NOT_FOUND = 255;
	public static final int APPLICATION_ID_NOT_FOUND = 255;
	public static final int VIRTUE_TEMPLATE_ID_NOT_FOUND = 255;
	public static final int VM_TEMPLATE_NOT_FOUND = 255;
	public static final int VM_NOT_FOUND = 255;
	public static final int NOT_YET_IMPLEMENTED = 255;
	public static final int USER_NOT_FOUND = 255;
	public static final int REQUESTED_USER_NOT_LOGGED_IN = 255;

	private int errorCode;

	public SaviorException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public SaviorException(int errorCode, String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
