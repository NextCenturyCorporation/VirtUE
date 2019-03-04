package com.nextcentury.savior.cifsproxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
 * Security configuration for using Active Directory for Authentication. Works
 * with the {@link DelegatingAuthenticationManager}.
 */
@EnableWebSecurity
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class ActiveDirectorySecurityConfig extends BaseSecurityConfig {
	protected ActiveDirectorySecurityConfig() {
		super("ActiveDirectory");
	}

	private static final XLogger logger = XLoggerFactory.getXLogger(ActiveDirectorySecurityConfig.class);

	/**
	 * The key for storing the username in the current {@link HttpSession}.
	 */
	public static final String USERNAME_ATTRIBUTE = "USERNAME";

	/**
	 * The key for storing the {@link Path} to the credential cache in the current
	 * {@link HttpSession}.
	 * 
	 * @see #serviceTicketFile
	 */
	public static final String CCACHE_PATH_ATTRIBUTE = "CCACHE_PATH";

	/**
	 * The key for storing the {@link Path} to the keytab in the current
	 * {@link HttpSession}.
	 * 
	 * @see #keytabLocation
	 */
	public static final String KEYTAB_PATH_ATTRIBUTE = "KEYTAB_PATH";

	/**
	 * The key for storing the service name in the current {@link HttpSession}
	 * (e.g., "http/webserver.test.savior").
	 */
	public static final String SERVICE_NAME_ATTRIBUTE = "SERVICE_NAME";

	/**
	 * 
	 * @return standard Spring resolver for property values (e.g., {@link Value}
	 *         annotation).
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Required: Active Directory security domain.
	 */
	@Value("${savior.security.ad.domain}")
	private String adDomain;

	/**
	 * Required: the URL to the active directory server
	 * 
	 * @see #getActiveDirectoryLdapAuthenticationProvider()
	 */
	@Value("${savior.security.ad.url}")
	private String adUrl;

	/**
	 * URL for the LDAP service
	 * 
	 * @see #kerberosLdapContextSource()
	 */
	@Value("${savior.security.ldap}")
	private String ldapURL;

	/**
	 * Required: The Active Directory principal the CIFS Proxy runs as, for example,
	 * "HTTP/bob-cifs-proxy@savior.com".
	 */
	@Value("${savior.cifsproxy.principal}")
	private String servicePrincipal;

	/**
	 * Location of the keytab for the {@link #servicePrincipal}. Defaults to
	 * "/etc/krb5.keytab".
	 */
	@Value("${savior.cifsproxy.keytab:/etc/krb5.keytab}")
	private File keytabLocation;

	/**
	 * Location of the temporary credential cache that will be used to stage creds
	 * for mounting filesystems.
	 */
	static Path serviceTicketFile;

	public ActiveDirectoryLdapAuthenticationProvider getActiveDirectoryLdapAuthenticationProvider() {
		ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(adDomain,
				adUrl);
		provider.setConvertSubErrorCodesToExceptions(true);
		provider.setUseAuthenticationRequestCredentials(true);
		return provider;
	}

	/**
	 * Checks that the attributes specified for {@link Value}s are ok (e.g.,
	 * required ones are set, existence of required files).
	 */
	@PostConstruct
	protected void validateValues() {
		if (adDomain == null || "".equals(adDomain)) {
			throw new IllegalArgumentException("domain must be specified");
		}
		if (adUrl == null || "".equals(adUrl)) {
			throw new IllegalArgumentException("url must be specified");
		}
		if (servicePrincipal == null || "".equals(servicePrincipal)) {
			throw new IllegalArgumentException("principal must be specified");
		}
		if (!keytabLocation.exists()) {
			throw new IllegalArgumentException("keytab '" + keytabLocation + "' does not exist");
		}
	}

	/**
	 * Creates the temporary credential cache file.
	 * 
	 * @return a {@link DelegatingAuthenticationManager} that delegates to the
	 *         superclass's {@link AuthenticationManager}
	 * @see BaseSecurityConfig#authenticationManagerBean()
	 * @see #serviceTicketFile
	 */
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		logger.entry();

		// we don't want anyone but us reading our ticket file
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
		FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
		serviceTicketFile = Files.createTempFile("cifsproxy-ccache", "", attr);

		AuthenticationManager authenticationManagerBean = new DelegatingAuthenticationManager(
				super.authenticationManagerBean(), serviceTicketFile);
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
				request.getSession().setAttribute(CCACHE_PATH_ATTRIBUTE, serviceTicketFile);
				request.getSession().setAttribute(KEYTAB_PATH_ATTRIBUTE, keytabLocation.toPath());
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
