package com.nextcentury.savior.cifsproxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Security configuration for using Active Directory for Authentication.
 */
@EnableWebSecurity
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class ActiveDirectorySecurityConfig extends BaseSecurityConfig {
	protected ActiveDirectorySecurityConfig() {
		super("ActiveDirectory");
	}

	private static final XLogger logger = XLoggerFactory.getXLogger(ActiveDirectorySecurityConfig.class);

	public static final String USERNAME_ATTRIBUTE = "USERNAME";

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Value("${savior.security.ad.domain:ERROR}")
	private String adDomain;

	@Value("${savior.security.ad.url:ERROR}")
	private String adUrl;

	@Value("${savior.security.ldap}")
	private String ldapURL;

	@Value("${savior.cifsproxy.principal}")
	private String servicePrincipal;

	@Value("${savior.cifsproxy.keytab:/etc/krb5.keytab}")
	private File keytabLocation;

	static public Path serviceTicketFile;

	private static final String TARGET_SERVICE_NAME = "cifs@fileserver.test.savior";
	
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

		// we don't want anyone but us reading our ticket file
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
		FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
		serviceTicketFile = Files.createTempFile("cifsproxy", "", attr);

		AuthenticationManager authenticationManagerBean = new DelegatingAuthenticationManager(
				super.authenticationManagerBean(), TARGET_SERVICE_NAME, serviceTicketFile);
		logger.exit(authenticationManagerBean);
		return authenticationManagerBean;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		logger.entry(auth);
		auth
				// .authenticationProvider(getActiveDirectoryLdapAuthenticationProvider())
				.authenticationProvider(kerberosServiceAuthenticationProvider())
				// for username/password
				.authenticationProvider(kerberosAuthenticationProvider());
		logger.exit();
	}

	// For username/password
	@Bean
	public KerberosAuthenticationProvider kerberosAuthenticationProvider() {
		KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
		SunJaasKerberosClient client = new SunJaasKerberosClient();
		client.setDebug(true);
		provider.setKerberosClient(client);
		provider.setUserDetailsService(userDetailsService());
		return provider;
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
		filter.setSuccessHandler(new AuthenticationSuccessHandler() {
			
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				request.getSession().setAttribute(USERNAME_ATTRIBUTE, authentication.getName());
			}
		});
		
		filter.setFailureHandler(new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
				response.setStatus(401);
				response.getWriter().write("401 - Access Denied");
				logger.debug("access denied", exception);
			}
		});
		logger.exit(filter);
		return filter;
	}

	@Bean
	public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
		logger.entry();
		KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
		provider.setTicketValidator(sunJaasKerberosTicketValidator());
		provider.setUserDetailsService(userDetailsService());
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
		http.csrf().disable();
	}
}
