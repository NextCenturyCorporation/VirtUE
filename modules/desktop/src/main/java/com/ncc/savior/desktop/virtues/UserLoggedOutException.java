package com.ncc.savior.desktop.virtues;

public class UserLoggedOutException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserLoggedOutException() {
	}

	public UserLoggedOutException(String message) {
       super(message);
    }
}
