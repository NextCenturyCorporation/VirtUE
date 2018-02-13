package com.ncc.savior.virtueadmin.config;

import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

public class ContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
		Properties props = new Properties();
		props.setProperty("spring.jersey.type", "filter");
		propertySources.addFirst(new PropertiesPropertySource("Jersey props", props));
	}

}
