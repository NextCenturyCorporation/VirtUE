package com.ncc.savior.virtueadmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationProvider demoAuthenticationProvider;

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
		auth.authenticationProvider(demoAuthenticationProvider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
//		 http.authorizeRequests().anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll();
		http.addFilterAt(new HeaderFilter(), AbstractPreAuthenticatedProcessingFilter.class);
		http.authorizeRequests().antMatchers("/**").hasRole("USER");
	}
}