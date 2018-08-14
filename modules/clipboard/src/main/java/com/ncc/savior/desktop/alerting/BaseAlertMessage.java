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
	protected String date;

	protected BaseAlertMessage(String title) {
		this.title = title;
		this.timeMillis = System.currentTimeMillis();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		this.date = formatter.format(date);
	}

	public long getTime() {
		return timeMillis;
	}
	
	public String getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public abstract String getPlainTextMessage();
}
