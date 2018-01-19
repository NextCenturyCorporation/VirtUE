/**
 * VirtueAdminApplication.java - Spring is initialized here.
 * 
 * 
 */
package com.ncc.savior.virtueadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;


/**
 * Applicaton Entry point
 */
@SpringBootApplication
@ImportResource("classpath:application-context.xml")
public class VirtueAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtueAdminApplication.class, args);
	}
}
