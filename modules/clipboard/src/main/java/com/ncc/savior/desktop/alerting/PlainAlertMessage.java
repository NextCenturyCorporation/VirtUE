package com.ncc.savior.desktop.alerting;

/**
 * An alert message implementation that just has a simple plain text message
 */
public class PlainAlertMessage extends BaseAlertMessage {
	private String message;

	public PlainAlertMessage(String title, String message) {
		super(title);
		this.message = message;
	}

	@Override
	public String getPlainTextMessage() {
		return message;
	}

}
