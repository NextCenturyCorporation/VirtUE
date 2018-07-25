package com.ncc.savior.util;

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


	private SaviorErrorCode errorCode;

	public SaviorException(SaviorErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public SaviorException(SaviorErrorCode errorCode, String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public SaviorErrorCode getErrorCode() {
		return errorCode;
	}
}
