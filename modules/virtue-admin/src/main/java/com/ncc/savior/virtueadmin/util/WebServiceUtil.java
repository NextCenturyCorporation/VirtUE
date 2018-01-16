package com.ncc.savior.virtueadmin.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created to facilitate a consistent error reporting and returning mechanism
 * throughout the system. We may need to modify this to fit with JHU's API.
 * 
 *
 */
public class WebServiceUtil implements ExceptionMapper<SaviorException> {
	private static final String HEADER_ERROR_CODE = "ErrorCode";
	private static final Logger logger = LoggerFactory.getLogger(WebServiceUtil.class);

	public static SaviorException createWebserviceException(Logger myLogger, String message, Exception e) {
		if (e instanceof SaviorException) {
			return (SaviorException) e;
		}
		if (myLogger != null) {
			myLogger.error(message, e);
		}
		SaviorException exception;
		if (message == null) {
			exception = new SaviorException(SaviorException.UNKNOWN_ERROR, "Unknown error", e);
		} else {
			exception = new SaviorException(SaviorException.UNKNOWN_ERROR, message, e);
		}
		return exception;
	}

	public static SaviorException createWebserviceException(String message, Exception e) {
		return createWebserviceException(logger, message, e);
	}

	public static SaviorException createWebserviceException(Exception e) {
		return createWebserviceException(logger, null, e);
	}

	public static SaviorException createWebserviceException(Logger myLogger, Exception e) {
		return createWebserviceException(myLogger, null, e);
	}

	@Override
	public Response toResponse(SaviorException exception) {
		// TODO need to be smarter
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if (exception.getLocalizedMessage() != null) {
			pw.println(exception.getLocalizedMessage());
		}
		exception.printStackTrace(pw);
		String message = "Error Code:" + exception.getErrorCode() + " " + sw.toString(); // stack trace as a string
		return Response.status(400).entity(message).header(HEADER_ERROR_CODE, exception.getErrorCode()).build();
	}
}
