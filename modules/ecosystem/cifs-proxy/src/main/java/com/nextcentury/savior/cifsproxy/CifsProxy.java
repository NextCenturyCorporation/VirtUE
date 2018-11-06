package com.nextcentury.savior.cifsproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Startup file for the CIFS Proxy. It proxies a CIFS/SMB filesystem
 * to a Virtue, applying Virtue-specific permsisions along the
 * way. Details are in the SAVIOR CIFS Proxy document.
 * 
 * @author clong
 *
 */
@SpringBootApplication
public class CifsProxy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(CifsProxy.class, args);
	}

}
