package com.ncc.savior.virtueadmin.util;

import com.ncc.savior.virtueadmin.util.SaviorException.ErrorCode;

/**
 * Generic exception for savior that could be passed to user or APIs
 * 
 *
 */
public class SaviorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public static enum ErrorCode {
		UNKNOWN_ERROR, INVALID_TEMPATE_ID, VIRTUE_ID_NOT_FOUND, APPLICATION_ID_NOT_FOUND, VIRTUE_TEMPLATE_ID_NOT_FOUND,
		VM_TEMPLATE_NOT_FOUND, VM_NOT_FOUND, NOT_YET_IMPLEMENTED, USER_NOT_FOUND, REQUESTED_USER_NOT_LOGGED_IN,
		VM_ERROR, CLOUD_ERROR;
		
		public SaviorException createException(String message) {
			return new SaviorException(this, message);
		}

		public SaviorException createException(String message, Throwable t) {
			return new SaviorException(this, message, t);
		}
	}

	private ErrorCode errorCode;

	public SaviorException(ErrorCode errorCode, String message) {
		this(errorCode, message, null);
	}

	public SaviorException(ErrorCode errorCode, String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
