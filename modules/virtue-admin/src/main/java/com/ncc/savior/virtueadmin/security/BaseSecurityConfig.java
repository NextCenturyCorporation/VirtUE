package com.ncc.savior.virtueadmin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.ncc.savior.virtueadmin.data.IUserManager;
import com.ncc.savior.virtueadmin.model.persistance.JpaVirtueUser;

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

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	@Autowired
	protected Environment env;

	@Autowired
	IUserManager userManager;

	protected BaseSecurityConfig(String type) {
		logger.info("Security configuration enabled. Type=" + type);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/").permitAll().antMatchers("/favicon.ico").permitAll()
				.antMatchers("/admin/**").hasRole(ADMIN_ROLE).antMatchers("/desktop/**").hasRole(USER_ROLE)
				.antMatchers("/data/**").permitAll().anyRequest().authenticated().and().formLogin().loginPage("/login")
				.permitAll().and().logout().permitAll();

		// http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

		http.sessionManagement().maximumSessions(10)
				// .invalidSessionUrl("/login")
				// .maximumSessions(1)
				.sessionRegistry(sessionRegistry()).expiredUrl("/login");
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

			JpaVirtueUser user = userManager.getUser(username);
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
