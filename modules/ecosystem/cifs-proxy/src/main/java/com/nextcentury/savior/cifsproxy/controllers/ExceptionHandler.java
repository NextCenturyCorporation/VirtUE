/**
 * 
 */
package com.nextcentury.savior.cifsproxy.controllers;

import javax.xml.ws.WebServiceException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Handle exceptions thrown by our controllers.
 * 
 * @author clong
 *
 */
public class ExceptionHandler extends ResponseEntityExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler({ WebServiceException.class })
	public ResponseEntity<Object> handleWebServiceException(Exception e, WebRequest request) {
		return new ResponseEntity<>("Server error: " + e, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
