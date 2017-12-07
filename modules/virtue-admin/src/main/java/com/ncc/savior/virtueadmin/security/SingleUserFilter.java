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

public class SingleUserFilter extends OncePerRequestFilter {

	private String username;
	private List<GrantedAuthority> authorities;

	public SingleUserFilter(Environment env) {
		username = env.getProperty("savior.security.singleuser.name");
		authorities = new ArrayList<GrantedAuthority>();
		String authoritiesString = env.getProperty("savior.security.singleuser.authorities");
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
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

}
