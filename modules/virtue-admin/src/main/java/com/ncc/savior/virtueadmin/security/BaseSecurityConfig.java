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
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.ncc.savior.virtueadmin.config.CorsFilter;
import com.ncc.savior.virtueadmin.data.IUserManager;

/**
 * Base security configuration for Savior Server. All other security
 * configurations should extend this one.
 */
public abstract class BaseSecurityConfig extends WebSecurityConfigurerAdapter {
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_CLASSPATH = "classpath:savior-server-security.properties";
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_WORKING_DIR = "file:savior-server-security.properties";

	private static Logger logger = LoggerFactory.getLogger(BaseSecurityConfig.class);
	protected static final String ADMIN_ROLE = "ADMIN";
	protected static final String USER_ROLE = "USER";

	private String[] csrfDisabledURLs;

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	@Autowired
	protected Environment env;

	@Autowired
	IUserManager userManager;

	protected BaseSecurityConfig(String type) {
		logger.info("Security configuration enabled. Type=" + type);
		csrfDisabledURLs = new String[] { "/desktop/**", "/data/**", "/login", "/logout" };
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				logger.debug("%%FAILED " + request.getRequestedSessionId());
				response.setStatus(401);
				response.getWriter().write("Login failure: " + exception.getMessage());
			}
		};
		AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {

				response.setStatus(200);
				response.setContentType(MediaType.APPLICATION_JSON.toString());

				response.getWriter().println("Login success");
			}
		};
		http
			.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers("/favicon.ico").permitAll()
				.antMatchers("/admin/**").hasRole(ADMIN_ROLE)
				.antMatchers(HttpMethod.OPTIONS,"/admin/**").permitAll()//allow CORS option calls
				.antMatchers(HttpMethod.OPTIONS,"/sensing/**").permitAll()//allow CORS option calls
				.antMatchers("/login").permitAll()
				.antMatchers("/logout").permitAll()
				.antMatchers("/desktop/**").hasRole(USER_ROLE)
				.antMatchers("/data/**").permitAll()// note this is a backdoor for development/testing.
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.failureHandler(authenticationFailureHandler)
				.successHandler(successHandler)
				.loginPage("/login")
				.and()
			.logout()
				.clearAuthentication(true)
				.deleteCookies("XSRF-TOKEN", "JSESSIONID")
				.invalidateHttpSession(true)
			;
		// http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringAntMatchers(csrfDisabledURLs);
		http.csrf().disable();

		http.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.maximumSessions(10).sessionRegistry(sessionRegistry()).expiredUrl("/login");

		http.addFilterBefore(new CorsFilter(env), ChannelProcessingFilter.class);

		doConfigure(http);

		if (forceHttps) {
			// sets port mapping for insecure to secure. Although this line isn't necessary
			// as it has 8080:8443 and 80:443 by default
			http.portMapper().http(8080).mapsTo(8443);
			// causes all requests to need to be over https.
			http.requiresChannel().anyRequest().requiresSecure();
		}
	}

	protected abstract void doConfigure(HttpSecurity http) throws Exception;

	protected void printDevModuleWarning(String authModuleName) {
		logger.warn("***** INSECURE AUTHORIZATION WARNING *****");
		logger.warn("Spring Security has been configured to use " + authModuleName
				+ " which is not intended for production use.  If you did not intend to use this module, please change the spring profile you are using!");
		logger.warn("***** INSECURE AUTHORIZATION WARNING *****");
	}

	protected AccessDeniedHandler getAccessDeniedHandler() {
		return new AccessDeniedHandler() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException) throws IOException, ServletException {
				response.setStatus(401);
				response.getWriter().write("401 - Access Denied - " + accessDeniedException.getLocalizedMessage());
				logger.debug("access denied", accessDeniedException);
			}
		};
	}

	@Override
	@Bean
	public DatabaseUserDetailsService userDetailsService() {
		return new DatabaseUserDetailsService();
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	class DatabaseUserDetailsService implements UserDetailsService {

		@Override
		public UserDetails loadUserByUsername(String fqdn) throws UsernameNotFoundException {
			String username = null;
			if (fqdn == null) {
				return cannotFindUser(username, "No username supplied");
			}
			if (fqdn.indexOf("@") != -1) {
				username = fqdn.substring(0, fqdn.indexOf("@"));
			}

			com.ncc.savior.virtueadmin.model.VirtueUser user = userManager.getUser(username);
			if (user == null) {
				return cannotFindUser(username, "Unable to find user=" + username + " in user database.");
			}
			Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
			for (String a : user.getAuthorities()) {
				authorities.add(new SimpleGrantedAuthority(a));
			}
			return new User(user.getUsername(), "notUsed", true, true, true, true, authorities);
		}

	}

	public User cannotFindUser(String username, String string) {
		logger.warn(string);
		return new User(username, "notUsed", true, true, true, true, new ArrayList<GrantedAuthority>(0));
	}

}
