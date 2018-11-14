package com.ncc.savior.util;

import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Utility methods for Loggers and logging.
 * 
 * @author clong
 *
 */
public class LoggerUtil {

	/**
	 * 
	 * @param logger
	 * @return the Level of the passed logger
	 */
	public static Level getLevel(Logger logger) {
		return logger.isTraceEnabled() ? Level.TRACE
				: logger.isDebugEnabled() ? Level.DEBUG
						: logger.isInfoEnabled() ? Level.INFO : logger.isWarnEnabled() ? Level.WARN : Level.ERROR;
	}

}
