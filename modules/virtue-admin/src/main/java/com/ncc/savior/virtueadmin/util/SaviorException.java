package com.ncc.savior.virtueadmin.util;

/**
 * Generic exception for savior that could be passed to user or APIs
 * 
 *
 */
public class SaviorException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int UNKNOWN_ERROR = 255;
	public static final int INVALID_TEMPATE_ID = 12;
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
