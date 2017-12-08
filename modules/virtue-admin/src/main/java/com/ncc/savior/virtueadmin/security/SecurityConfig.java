package com.ncc.savior.virtueadmin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.ncc.savior.virtueadmin.util.SaviorException;

/**
 * A property configurable Spring Security configuration. There are multiple
 * modes for authentication that can be set via the
 * 'savior.security.authentication' property.
 * <ul>
 * <li>HEADER - All connections will be granted the username and roles from
 * specific headers in the request. See {@link HeaderFilter} for details. The
 * client is fully trusted and thus this mode should not be used in production.
 * <li>SINGLEUSER - All connections will be granted the username and roles of a
 * user configured the security property file. This mode should not be used in
 * production. See {@link SingleUserFilter} for details.
 * <li>ACTIVEDIRECTORY - not fully implemented
 * <li>LDAP - not fully implemented
 * 
 *
 */
@EnableWebSecurity
@PropertySource(SecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES = "classpath:savior-server-security.properties";

	private static final String AUTH_MODULE_LDAP = "LDAP";

	private static final String AUTH_MODULE_ACTIVEDIRECTORY = "ACTIVEDIRECTORY";

	private static final String AUTH_MODULE_SINGLEUSER = "SINGLEUSER";

	private static final String AUTH_MODULE_DUMMY = "HEADER";

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	private static final String PROPERTY_AUTH_MODULE = "savior.security.authentication";

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Autowired
	Environment env;

	@Value("${" + PROPERTY_AUTH_MODULE + ":ERROR}")
	private String authModuleName;

	@Value("${savior.security.ad.domain:ERROR}")
	private String adDomain;

	@Value("${savior.security.ad.url:ERROR}")
	private String adUrl;

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	public ActiveDirectoryLdapAuthenticationProvider getActiveDirectoryLdapAuthenticationProvider() {
		ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(adDomain,
				adUrl);
		provider.setConvertSubErrorCodesToExceptions(true);
		provider.setUseAuthenticationRequestCredentials(true);
		return provider;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// auth.ldapAuthentication().userDnPatterns("uid={0},ou=people").groupSearchBase("ou=groups");
		// auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
		// auth.inMemoryAuthentication().withUser("user2").password("password").roles("USER");
		// auth.inMemoryAuthentication().withUser("user3").password("password").roles("USER");
		// auth.inMemoryAuthentication().withUser("admin").password("password").roles("USER",
		// "ADMIN");
		// auth.inMemoryAuthentication().withUser(User.testUser().getUsername()).password("").roles("USER");
		// auth.inMemoryAuthentication().withUser("kdrumm").roles("USER", "ADMIN");

		if (authModuleName.equalsIgnoreCase(AUTH_MODULE_DUMMY)) {
			auth.authenticationProvider(new PassThroughAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase(AUTH_MODULE_SINGLEUSER)) {
			auth.authenticationProvider(new PassThroughAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase(AUTH_MODULE_ACTIVEDIRECTORY)) {
			auth.authenticationProvider(getActiveDirectoryLdapAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase(AUTH_MODULE_LDAP)) {
			auth.ldapAuthentication().userDnPatterns("uid={0},ou=people").groupSearchBase("ou=groups");
		} else {
			throw new SaviorException(-1,
					"Configuration error!  Need to set 'savior.security.authentication' in security property file");
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// http.authorizeRequests().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll();
		if (authModuleName.equalsIgnoreCase(AUTH_MODULE_DUMMY)) {
			http.addFilterAt(new HeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
			printDevModuleWarning(authModuleName);
		} else if (authModuleName.equalsIgnoreCase(AUTH_MODULE_SINGLEUSER)) {
			http.addFilterAt(new SingleUserFilter(env), AbstractPreAuthenticatedProcessingFilter.class);
			printDevModuleWarning(authModuleName);
		}
		// Set what roles are required to view each urls
		http.authorizeRequests().antMatchers("/desktop/**").hasRole("USER").antMatchers("/admin/**").hasRole("ADMIN")
				.antMatchers("/").permitAll();
		http.exceptionHandling().accessDeniedHandler(getAccessDeniedHandler());

		if (forceHttps) {
			// sets port mapping for insecure to secure. Although this line isn't necessary
			// as it has 8080:8443 and 80:443 by default
			http.portMapper().http(8080).mapsTo(8443);
			// causes all requests to need to be over https.
			http.requiresChannel().anyRequest().requiresSecure();
		}
	}

	private void printDevModuleWarning(String authModuleName) {
		logger.warn("***** INSECURE AUTHORIZATION WARNING *****");
		logger.warn("Spring Security has been configured to use " + authModuleName
				+ " which is not intended for production use.  If you did not intend to use this module, please change the '"
				+ PROPERTY_AUTH_MODULE + "' property in your security property file (usually "
				+ DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES + ").");
		logger.warn("***** INSECURE AUTHORIZATION WARNING *****");
	}

	private AccessDeniedHandler getAccessDeniedHandler() {
		return new AccessDeniedHandler() {
			@Override
			public void handle(HttpServletRequest request, HttpServletResponse response,
					AccessDeniedException accessDeniedException) throws IOException, ServletException {
				response.setStatus(401);
				response.getWriter().write("401 - Access Denied");
			}
		};
	}
}