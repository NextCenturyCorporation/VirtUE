package com.ncc.savior.virtueadmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("default-alert")
@Component
public class DefaultProfileWarner {
	private static final Logger logger = LoggerFactory.getLogger(DefaultProfileWarner.class);

	public DefaultProfileWarner() {
		logger.warn("\n\t***WARNING: Using default profile! (This is ok for development)***"
				+ "\n\t*  If you want to use a customized profile, you can set your profiles via:"
				+ "\n\t\t1. Create an application.properties file in your working directory and add a 'spring.profiles.active' property"
				+ "\n\t\t2. Add '--spring.profiles.active=' as a command line arugment"
				+ "\n\t Profile lists can be comma seperated");

	}
}
