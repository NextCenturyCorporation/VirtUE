package com.ncc.savior.virtueadmin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;

public abstract class BaseSecurityConfig extends WebSecurityConfigurerAdapter {
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES = "classpath:savior-server-security.properties";
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES2 = "file:savior-server-security-site.properties";

	private static Logger logger = LoggerFactory.getLogger(BaseSecurityConfig.class);
	protected static final String ADMIN_ROLE = "ADMIN";
	protected static final String USER_ROLE = "USER";

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;
	private String type;

	@Autowired
	protected Environment env;

	protected BaseSecurityConfig(String type) {
		this.type = type;
		logger.info("Security configuration enabled. Type=" + type);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/").authenticated().antMatchers("/admin/**").hasRole(ADMIN_ROLE)
				.antMatchers("/desktop/**").hasRole(USER_ROLE).antMatchers("/data/**").permitAll().anyRequest()
				.authenticated().and().formLogin().loginPage("/login").permitAll().and().logout().permitAll();

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
				response.getWriter().write("401 - Access Denied");
			}
		};
	}

	static class DummyUserDetailsService implements UserDetailsService {

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			return new User(username, "notUsed", true, true, true, true,
					AuthorityUtils.createAuthorityList("ROLE_USER"));
		}

	}
}
