package com.nextcentury.savior.cifsproxy;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Base security configuration for Savior Server. All other security
 * configurations should extend this one.
 */
public abstract class BaseSecurityConfig extends WebSecurityConfigurerAdapter {
	/**
	 * The cifs proxy doesn't need any user info, so fake it.
	 * 
	 * @author clong
	 *
	 */
	protected static class FakeUserDetailsService implements UserDetailsService {

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			return new User(username, "", Collections.emptySet());
		}

	}

	public static final String DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH = "classpath:cifs-proxy-security.properties";
	public static final String DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR = "file:cifs-proxy-security.properties";

	private static Logger logger = LoggerFactory.getLogger(BaseSecurityConfig.class);

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	@Autowired
	protected Environment env;

	protected BaseSecurityConfig(String type) {
		logger.info("Security configuration enabled. Type=" + type);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				response.setStatus(401);
				response.getWriter().write("Login failure: " + exception.getMessage());
			}
		};
		AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				response.setStatus(200);
				response.getWriter().println("Login success");
			}
		};
		http.authorizeRequests().antMatchers("/").permitAll().antMatchers("/favicon.ico").permitAll()
				.antMatchers("/hello").permitAll().antMatchers("/data/**").permitAll().anyRequest().authenticated()
				.and().formLogin().failureHandler(authenticationFailureHandler).successHandler(successHandler)
				.loginPage("/login").permitAll().and().logout().permitAll();

		// http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

		doConfigure(http);

		http.csrf().disable();
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
	public UserDetailsService userDetailsService() {
		return new FakeUserDetailsService();
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

}
