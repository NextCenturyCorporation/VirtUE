package com.ncc.savior.desktop.authorization;

import java.io.IOException;

public class InvalidUserLoginException extends IOException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidUserLoginException(String message) {
		super(message);
	}

}
