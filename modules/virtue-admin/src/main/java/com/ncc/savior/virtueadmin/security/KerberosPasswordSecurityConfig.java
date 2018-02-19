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
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig;
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Profile({ "kpw", "kerberos-password" })
@EnableWebSecurity
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class KerberosPasswordSecurityConfig extends BaseSecurityConfig {
	protected KerberosPasswordSecurityConfig() {
		super("Kerberos-password");
	}

	private static final XLogger logger = XLoggerFactory.getXLogger(KerberosPasswordSecurityConfig.class);

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
//
//	@Value("${savior.security.ad.domain:ERROR}")
//	private String adDomain;
//
//	@Value("${savior.security.ad.url:ERROR}")
//	private String adUrl;
//
//	@Value("${savior.security.https.force:false}")
//	private boolean forceHttps;
//
//	@Value("${savior.security.ldap}")
//	private String ldapURL;

	@Value("${savior.virtueadmin.principal}")
	private String servicePrincipal;

	@Value("${savior.virtueadmin.keytab}")
	private File keytabLocation;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.authenticationProvider(kerberosAuthenticationProvider());
	}

	@Bean
	public KerberosAuthenticationProvider kerberosAuthenticationProvider() {
		KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
		SunJaasKerberosClient client = new SunJaasKerberosClient();
		client.setDebug(true);
		provider.setKerberosClient(client);
		provider.setUserDetailsService(new DatabaseUserDetailsService());
		return provider;
	}

	@Override
	protected void doConfigure(HttpSecurity http) throws Exception {
		//do nothing extra
	}

}
