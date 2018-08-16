package com.ncc.savior.desktop.alerting;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Base class for alert messages that handles some common functionality such as
 * storing the time the alert was created.
 * 
 */
public abstract class BaseAlertMessage {
	protected long timeMillis;
	protected String title;

	protected BaseAlertMessage(String title) {
		this.title = title;
		this.timeMillis = System.currentTimeMillis();
	}

	public long getTime() {
		return timeMillis;
	}

	public String getTitle() {
		return title;
	}

	public abstract String getPlainTextMessage();
}
