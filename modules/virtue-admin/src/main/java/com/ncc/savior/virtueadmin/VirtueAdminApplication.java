package com.ncc.savior.virtueadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.ImportResource;


/**
 * Applicaton Entry point
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class, SessionAutoConfiguration.class })
@ImportResource("classpath:application-context.xml")
public class VirtueAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtueAdminApplication.class, args);
	}
}
