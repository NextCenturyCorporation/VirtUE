/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@Provider
public class CorsFilter implements ContainerResponseFilter, Filter {
	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	private Boolean enabled;
	private String allowOrigin;
	private String allowHeaders;
	private String allowCredentials;
	private String allowMethods;

	/** see note in BaseSecurityConfig about the result of requests made to Spring endpoints (like /login), when CORS is set up improperly */
	@Autowired
	public CorsFilter(Environment env) {
		this.enabled = Boolean.valueOf(env.getProperty("savior.cors.enabled", "false"));
		this.allowOrigin = env.getProperty("savior.cors.allow-origin", "*");
		this.allowHeaders = env.getProperty("savior.cors.allow-headers", "origin, content-type, accept, authorization");
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

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//do nothing
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		if (enabled) {
//			HttpServletRequest httpReq = (HttpServletRequest) servletRequest;
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			response.setHeader("Access-Control-Allow-Origin", allowOrigin);
			response.setHeader("Access-Control-Allow-Headers", allowHeaders);
			response.setHeader("Access-Control-Allow-Credentials", allowCredentials);
			response.setHeader("Access-Control-Allow-Methods", allowMethods);
		}
		chain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {
		//do nothing
	}
}
