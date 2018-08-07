package com.ncc.savior.desktop.alerting;

import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.datetime.DateFormatter;

/**
 * Alerting service that just displays a plain text rendition of any alert. This
 * implementation is not overly useful for production systems.
 * 
 *
 */
public class LoggingAlertService implements IUserAlertService {
	private static final Logger logger = LoggerFactory.getLogger(LoggingAlertService.class);

	private DateFormatter dateFormatter;

	private Locale locale;

	public LoggingAlertService(String dateFormatString) {
		this.dateFormatter = new DateFormatter(dateFormatString);
	}

	public LoggingAlertService() {
		this.dateFormatter = new DateFormatter();
	}

	@Override
	public void displayAlert(BaseAlertMessage alertMessage) {
		StringBuilder sb = new StringBuilder();
		Date date = new Date(alertMessage.timeMillis);
		String formattedDate = dateFormatter.print(date, locale);
		sb.append("ALERT: ");
		sb.append(formattedDate).append(" : ");
		sb.append(alertMessage.getTitle()).append(" - ");
		sb.append(alertMessage.getPlainTextMessage());
		logger.info(sb.toString());
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

}
