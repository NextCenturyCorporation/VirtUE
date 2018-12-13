package com.ncc.savior.virtueadmin.config;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

@Provider
@Order(-100000)
public class CorsFilter implements ContainerResponseFilter {
	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	private Boolean enabled;
	private String allowOrigin;
	private String allowHeaders;
	private String allowCredentials;
	private String allowMethods;

	@Autowired
	public CorsFilter(Environment env) {
		this.enabled = Boolean.valueOf(env.getProperty("savior.cors.enabled", "false"));
		this.allowOrigin = env.getProperty("savior.cors.allow-origin", "*");
		this.allowHeaders = env.getProperty("savior.cors.allow-headers", "origin, content-type, accept, authorization, responseType");
		this.allowCredentials = env.getProperty("savior.cors.allow-credentials", "true");
		this.allowMethods = env.getProperty("savior.cors.allow-methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		if (enabled) {
			logger.debug("CORS Filter has been enabled");
			logger.debug("  CORS-allow-origin=" + allowOrigin);
			logger.debug("  CORS-allow-headers=" + allowHeaders);
			logger.debug("  CORS-allow-credentials=" + allowCredentials);
			logger.debug("  CORS-allow-methods=" + allowMethods);
		}
	}

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		if (enabled) {
			response.getHeaders().add("Access-Control-Allow-Origin", allowOrigin);
			response.getHeaders().add("Access-Control-Allow-Headers", allowHeaders);
			response.getHeaders().add("Access-Control-Allow-Credentials", allowCredentials);
			response.getHeaders().add("Access-Control-Allow-Methods", allowMethods);
		}
	}
}
