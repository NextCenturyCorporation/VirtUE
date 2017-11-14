package com.ncc.savior.virtueadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class VirtueAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtueAdminApplication.class, args);
	}
	
	
}
