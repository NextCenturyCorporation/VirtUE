package com.ncc.savior.virtueadmin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

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
@Profile("header")
@EnableWebSecurity
@PropertySources({ @PropertySource(HeaderSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = HeaderSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class HeaderSecurityConfig extends BaseSecurityConfig {

	protected HeaderSecurityConfig() {
		super("Header");
	}

	private static final Logger logger = LoggerFactory.getLogger(HeaderSecurityConfig.class);

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	protected void doConfigure(HttpSecurity http) throws Exception {
		http.addFilterAt(new HeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
		printDevModuleWarning("HEADER");
		http.exceptionHandling().accessDeniedHandler(getAccessDeniedHandler());
	}

	@Autowired
	private void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new PassThroughAuthenticationProvider());
	}



}
