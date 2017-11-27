package com.ncc.savior.virtueadmin.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created to facilitate a consistent error reporting and returning mechanism
 * throughout the system. We may need to modify this to fit with JHU's API.
 * 
 *
 */
public class WebServiceUtil implements ExceptionMapper<WebServiceException>{
	private static final Logger logger = LoggerFactory.getLogger(WebServiceUtil.class);

	public static WebServiceException createWebserviceException(Logger myLogger, String message, Exception e) {
		if (myLogger != null) {
			myLogger.error(message, e);
		}
		WebServiceException exception;
		if (message == null) {
			exception = new WebServiceException(e);
		} else {
			exception = new WebServiceException(message, e);
		}
		return exception;
	}

	public static WebServiceException createWebserviceException(String message, Exception e) {
		return createWebserviceException(logger, message, e);
	}

	public static WebServiceException createWebserviceException(Exception e) {
		return createWebserviceException(logger, null, e);
	}

	public static WebServiceException createWebserviceException(Logger myLogger, Exception e) {
		return createWebserviceException(myLogger, null, e);
	}

	@Override
	public Response toResponse(WebServiceException exception) {
		//TODO need to be smarter
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		if (exception.getLocalizedMessage()!=null) {
			pw.println(exception.getLocalizedMessage());
		}
		exception.printStackTrace(pw);
		String message = sw.toString(); // stack trace as a string
		return Response.status(400).entity(message).build();
	}
}
