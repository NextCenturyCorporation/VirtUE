package com.nextcentury.savior.cifsproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.ncc.savior.virtueadmin.template.FreeMarkerTemplateService;

/**
 * Startup file for the CIFS Proxy. It proxies a CIFS/SMB filesystem
 * to a Virtue, applying Virtue-specific permsisions along the
 * way. Details are in the SAVIOR CIFS Proxy document.
 * 
 * @author clong
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class CifsProxy {

	@Value("${savior.cifsproxy.templateDir:templates}")
	private String templateDir;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(CifsProxy.class, args);
	}

	@Bean
	public FreeMarkerTemplateService templateService() {
		return new FreeMarkerTemplateService(templateDir);
	}
}
