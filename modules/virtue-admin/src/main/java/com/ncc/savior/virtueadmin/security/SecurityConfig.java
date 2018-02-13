package com.ncc.savior.virtueadmin.security;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
@PropertySources({ @PropertySource(SecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES),
		@PropertySource(value = SecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES2, ignoreResourceNotFound = true) })
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES = "classpath:savior-server-security.properties";
	protected static final String DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES2 = "file:savior-server-security-site.properties";
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(SecurityConfig.class);

	private static final String AUTH_MODULE_LDAP = "LDAP";

	private static final String AUTH_MODULE_ACTIVEDIRECTORY = "ACTIVEDIRECTORY";

	private static final String AUTH_MODULE_SINGLEUSER = "SINGLEUSER";

	private static final String AUTH_MODULE_DUMMY = "HEADER";

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	private static final String PROPERTY_AUTH_MODULE = "savior.security.authentication";
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String USER_ROLE = "USER";

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
		LOGGER.entry();
		AuthenticationManager authenticationManagerBean = super.authenticationManagerBean();
		LOGGER.exit(authenticationManagerBean);
		return authenticationManagerBean;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.entry(http);
		if (authModuleName.equalsIgnoreCase(AUTH_MODULE_DUMMY)) {
			http.addFilterAt(new HeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
			printDevModuleWarning(authModuleName);
		} else if (authModuleName.equalsIgnoreCase(AUTH_MODULE_SINGLEUSER)) {
			http.addFilterAt(new SingleUserFilter(env), AbstractPreAuthenticatedProcessingFilter.class);
			printDevModuleWarning(authModuleName);
		}
		
		http.exceptionHandling().authenticationEntryPoint(spnegoEntryPoint()).accessDeniedHandler(getAccessDeniedHandler()).and().authorizeRequests().antMatchers("/")
				.authenticated().antMatchers("/admin/**").hasRole(ADMIN_ROLE).antMatchers("/desktop/**")
				.hasRole(USER_ROLE).antMatchers("/data/**").permitAll().anyRequest().authenticated().and().formLogin()
				.loginPage("/login").permitAll().and().logout().permitAll().and()
				.addFilterBefore(spnegoAuthenticationProcessingFilter(authenticationManagerBean()),
						BasicAuthenticationFilter.class);
		
		if (forceHttps) {
			// sets port mapping for insecure to secure. Although this line isn't necessary
			// as it has 8080:8443 and 80:443 by default
			http.portMapper().http(8080).mapsTo(8443);
			// causes all requests to need to be over https.
			http.requiresChannel().anyRequest().requiresSecure();
		}
		LOGGER.exit();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		LOGGER.entry(auth);
		auth.authenticationProvider(getActiveDirectoryLdapAuthenticationProvider())
				.authenticationProvider(kerberosServiceAuthenticationProvider());
		LOGGER.exit();
	}

	@Bean
	public SpnegoEntryPoint spnegoEntryPoint() {
		LOGGER.entry();
		SpnegoEntryPoint spnegoEntryPoint = new SpnegoEntryPoint();
		LOGGER.exit(spnegoEntryPoint);
		return spnegoEntryPoint;
	}

	@Bean
	public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(
			AuthenticationManager authenticationManager) {
		LOGGER.entry(authenticationManager);
		SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
		filter.setAuthenticationManager(authenticationManager);
		LOGGER.exit(filter);
		return filter;
	}

	@Bean
	public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
		LOGGER.entry();
		KerberosServiceAuthenticationProvider provider = new KerberosServiceAuthenticationProvider();
		provider.setTicketValidator(sunJaasKerberosTicketValidator());
		provider.setUserDetailsService(new DummyUserDetailsService());
		LOGGER.exit(provider);
		return provider;
	}

	@Bean
	public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
		LOGGER.entry();
		SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
		ticketValidator.setServicePrincipal(servicePrincipal);
		ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
		ticketValidator.setDebug(true);
		LOGGER.exit(ticketValidator);
		return ticketValidator;
	}

	@Bean
	public KerberosLdapContextSource kerberosLdapContextSource() {
		LOGGER.entry();
		KerberosLdapContextSource contextSource = new KerberosLdapContextSource(ldapURL);
		contextSource.setLoginConfig(loginConfig());
		LOGGER.exit(contextSource);
		return contextSource;
	}

	@Bean
	public SunJaasKrb5LoginConfig loginConfig() {
		LOGGER.entry();
		SunJaasKrb5LoginConfig loginConfig = new SunJaasKrb5LoginConfig();
		loginConfig.setKeyTabLocation(new FileSystemResource(keytabLocation));
		loginConfig.setServicePrincipal(servicePrincipal);
		loginConfig.setDebug(true);
		loginConfig.setIsInitiator(true);
		LOGGER.exit(loginConfig);
		return loginConfig;
	}

	@Autowired
	private void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
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
			// auth.authenticationProvider(getActiveDirectoryLdapAuthenticationProvider());
		} else {
			throw new SaviorException(-1,
					"Configuration error!  Need to set 'savior.security.authentication' in security property file");
		}
	}

	private void printDevModuleWarning(String authModuleName) {
		logger.warn("***** INSECURE AUTHORIZATION WARNING *****");
		logger.warn("Spring Security has been configured to use " + authModuleName
				+ " which is not intended for production use.  If you did not intend to use this module, please change the '"
				+ PROPERTY_AUTH_MODULE + "' property in your security property file (usually "
				+ DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES + " or " + DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES2
				+ ").");
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

	static class DummyUserDetailsService implements UserDetailsService {

		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			return new User(username, "notUsed", true, true, true, true,
					AuthorityUtils.createAuthorityList("ROLE_USER"));
		}

	}

}
