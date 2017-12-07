package com.ncc.savior.virtueadmin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@EnableWebSecurity
@PropertySource("classpath:savior-server-security.properties")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Autowired
	Environment env;

	@Value("${savior.security.authentication:ERROR}")
	private String authModuleName;

	@Value("${savior.security.ad.domain:ERROR}")
	private String adDomain;

	@Value("${savior.security.ad.url:ERROR}")
	private String adUrl;

	@Value("${savior.security.https.force:false}")
	private boolean forceHttps;

	@Bean
	public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
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

		if (authModuleName.equalsIgnoreCase("DUMMY")) {
			auth.authenticationProvider(new PassThroughAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase("SINGLEUSER")) {
			auth.authenticationProvider(new PassThroughAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase("ACTIVEDIRECTORY")) {
			auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
		} else if (authModuleName.equalsIgnoreCase("LDAP")) {
			auth.ldapAuthentication().userDnPatterns("uid={0},ou=people").groupSearchBase("ou=groups");
		} else {
			throw new SaviorException(-1,
					"Configuration error!  Need to set 'savior.security.authentication' in security property file");
		}
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// http.authorizeRequests().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll();
		if (authModuleName.equalsIgnoreCase("DUMMY")) {
			http.addFilterAt(new HeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
		} else if (authModuleName.equalsIgnoreCase("SINGLEUSER")) {
			http.addFilterAt(new SingleUserFilter(env), AbstractPreAuthenticatedProcessingFilter.class);
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