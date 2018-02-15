package com.ncc.savior.virtueadmin.security;

import java.io.File;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
@Profile({ "ad", "activedirectory" })
@EnableWebSecurity
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class ActiveDirectorySecurityConfig extends BaseSecurityConfig {
	protected ActiveDirectorySecurityConfig() {
		super("ActiveDirectory");
	}

	private static final XLogger logger = XLoggerFactory.getXLogger(ActiveDirectorySecurityConfig.class);

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Value("${savior.security.ad.domain:ERROR}")
	private String adDomain;

	@Value("${savior.security.ad.url:ERROR}")
	private String adUrl;

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	@Value("${savior.security.ldap}")
	private String ldapURL;

	@Value("${savior.virtueadmin.principal}")
	private String servicePrincipal;

	@Value("${savior.virtueadmin.keytab}")
	private File keytabLocation;

	public ActiveDirectoryLdapAuthenticationProvider getActiveDirectoryLdapAuthenticationProvider() {
		ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(adDomain,
				adUrl);
		provider.setConvertSubErrorCodesToExceptions(true);
		provider.setUseAuthenticationRequestCredentials(true);
		return provider;
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		logger.entry();
		AuthenticationManager authenticationManagerBean = super.authenticationManagerBean();
		logger.exit(authenticationManagerBean);
		return authenticationManagerBean;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		logger.entry(auth);
		auth.authenticationProvider(getActiveDirectoryLdapAuthenticationProvider())
				.authenticationProvider(kerberosServiceAuthenticationProvider());
		logger.exit();
	}

	@Bean
	public SpnegoEntryPoint spnegoEntryPoint() {
		logger.entry();
		SpnegoEntryPoint spnegoEntryPoint = new SpnegoEntryPoint();
		logger.exit(spnegoEntryPoint);
		return spnegoEntryPoint;
	}

	@Bean
	public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
			AuthenticationManager authenticationManager) {
		logger.entry(authenticationManager);
		SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
		filter.setAuthenticationManager(authenticationManager);
		logger.exit(filter);
		return filter;
	}

	@Bean
	public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
		logger.entry();
		KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
		provider.setTicketValidator(sunJaasKerberosTicketValidator());
		provider.setUserDetailsService(new DummyUserDetailsService());
		logger.exit(provider);
		return provider;
	}

	@Bean
	public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
		logger.entry();
		SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
		ticketValidator.setServicePrincipal(servicePrincipal);
		ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
		ticketValidator.setDebug(true);
		logger.exit(ticketValidator);
		return ticketValidator;
	}

	@Bean
	public KerberosLdapContextSource kerberosLdapContextSource() {
		logger.entry();
		KerberosLdapContextSource contextSource = new KerberosLdapContextSource(ldapURL);
		contextSource.setLoginConfig(loginConfig());
		logger.exit(contextSource);
		return contextSource;
	}

	@Bean
	public SunJaasKrb5LoginConfig loginConfig() {
		logger.entry();
		SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
		loginConfig.setKeyTabLocation(new FileSystemResource(keytabLocation));
		loginConfig.setServicePrincipal(servicePrincipal);
		loginConfig.setDebug(true);
		loginConfig.setIsInitiator(true);
		logger.exit(loginConfig);
		return loginConfig;
	}

	@Override
	protected void doConfigure(HttpSecurity http) throws Exception {
		http.exceptionHandling().authenticationEntryPoint(spnegoEntryPoint())
				.accessDeniedHandler(getAccessDeniedHandler()).and()
				.addFilterBefore(spnegoAuthenticationProcessingFilter(authenticationManagerBean()),
						BasicAuthenticationFilter.class);

	}

}