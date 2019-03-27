/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
 * Security configuration where all connections will be granted the username and
 * roles of a user configured the security property file. This mode should not
 * be used in production. See {@link SingleUserFilter} for details.
 */
@Profile({ "singleuser", "default" })
@EnableWebSecurity
@PropertySources({ @PropertySource(HeaderSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = HeaderSecurityConfig.DEFAULT_SAVIOR_SERVER_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class SingleUserSecurityConfig extends BaseSecurityConfig {

	protected SingleUserSecurityConfig() {
		super("SingleUser");
	}

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SingleUserSecurityConfig.class);

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	protected void doConfigure(HttpSecurity http) throws Exception {
		http.addFilterAt(new SingleUserFilter(env), AbstractPreAuthenticatedProcessingFilter.class);
		printDevModuleWarning("SINGLEUSER");
		http.exceptionHandling().accessDeniedHandler(getAccessDeniedHandler());
	}

	@Autowired
	private void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(new PassThroughAuthenticationProvider());
	}

}
