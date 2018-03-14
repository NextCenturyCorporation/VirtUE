package com.ncc.savior.virtueadmin.config;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicatonExceptionMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException ex) {
//		int status = ex.getResponse().getStatus();
		return ex.getResponse();
	}

}