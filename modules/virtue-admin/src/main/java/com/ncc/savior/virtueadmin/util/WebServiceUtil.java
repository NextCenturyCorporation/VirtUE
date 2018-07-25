package com.ncc.savior.virtueadmin.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.util.SaviorErrorCode;
import com.ncc.savior.util.SaviorException;

/**
 * Created to facilitate a consistent error reporting and returning mechanism
 * throughout the system. We may need to modify this to fit with JHU's API.
 */
public class WebServiceUtil implements ExceptionMapper<Exception> {
	public static final String HEADER_ERROR_CODE = "ErrorCode";
	public static final String HEADER_ERROR_CODE_STRING = "ErrorCodeString";
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
			exception = new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, "Unknown error", e);
		} else {
			exception = new SaviorException(SaviorErrorCode.UNKNOWN_ERROR, message, e);
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
	public Response toResponse(Exception exception) {
		SaviorErrorCode errorCode = SaviorErrorCode.UNKNOWN_ERROR;
		if (exception instanceof SaviorException) {
			SaviorException se = (SaviorException) exception;
			if (se != null && se.getErrorCode() != null) {
				errorCode = se.getErrorCode();
			}
		}
		int httpCode = errorCode.getHttpResponseCode();
		String message = createResponseTextFromErrorCode(errorCode, exception);
		ResponseBuilder builder = Response.status(httpCode).entity(message);
		builder.header(HEADER_ERROR_CODE, errorCode.getErrorCode());
		builder.header(HEADER_ERROR_CODE_STRING, errorCode.getReadableString());
		return builder.build();
	}

	public static String getStacktraceString(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		return sw.toString();
	}

	public static String createResponseTextFromErrorCode(SaviorErrorCode errorCode, Exception exception) {
		StringBuilder sb = new StringBuilder();
		sb.append("Error Code: ");
		sb.append(errorCode.getErrorCode()).append(" - ").append(errorCode.getReadableString());
		sb.append("\n");
		sb.append("Exception Message: ");
		sb.append(exception.getLocalizedMessage());
		sb.append("\n");
		sb.append("\n");
		sb.append("Exception:");
		sb.append("\n");
		// add stack trace
		String stacktrace = getStacktraceString(exception);
		sb.append(stacktrace.toString());
		String message = sb.toString();
		return message;
	}
}
