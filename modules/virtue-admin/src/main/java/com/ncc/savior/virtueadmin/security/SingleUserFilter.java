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
package com.ncc.savior.virtueadmin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that injects user and roles from a property file to the user object
 * for Spring Security. ALL requests will be granted this user if this module is
 * used. Do NOT use in production.
 */
public class SingleUserFilter extends OncePerRequestFilter {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SingleUserFilter.class);

	private static final String PROPERTY_SINGLEUSER_AUTHORITIES = "savior.security.singleuser.authorities";
	private static final String PROPERTY_SINGLEUSER_NAME = "savior.security.singleuser.name";
	private String username;
	private List<GrantedAuthority> authorities;

	public SingleUserFilter(Environment env) {
		username = env.getProperty(PROPERTY_SINGLEUSER_NAME);
		authorities = new ArrayList<GrantedAuthority>();
		String authoritiesString = env.getProperty(PROPERTY_SINGLEUSER_AUTHORITIES);
		for (String a : authoritiesString.split(",")) {
			authorities.add(new SimpleGrantedAuthority(a));
		}
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		;
		// String temp = request
		// if (request.getSession(false) != null) {
		// 	logger.debug("singleUser " + request.getSession(false).getId());
		// }

		Authentication authentication = new AbstractAuthenticationToken(authorities) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getPrincipal() {
				return username;
			}

			@Override
			public Object getCredentials() {
				return username;
			}

		};
		authentication.setAuthenticated(true);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

}
