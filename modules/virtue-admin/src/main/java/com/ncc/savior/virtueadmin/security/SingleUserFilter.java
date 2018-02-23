package com.ncc.savior.virtueadmin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
