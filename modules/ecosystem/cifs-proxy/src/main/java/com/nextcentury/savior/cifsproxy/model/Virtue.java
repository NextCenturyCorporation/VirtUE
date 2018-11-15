package com.nextcentury.savior.cifsproxy.model;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * The representation of a Virtue for the CIFS Proxy.
 * 
 * @author clong
 *
 */
public class Virtue {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(Virtue.class);

	private String name;
	private String username;
	private String password;

	/**
	 * 
	 * @param name
	 *                     the name of the Virtue
	 * @param username
	 *                     the username to use to log in. Must be a valid username
	 *                     for the system the Proxy runs on.
	 */
	public Virtue(String name, String username, String password) {
		super();
		LOGGER.entry(name, username);
		this.name = name;
		this.username = username;
		this.password = password;
		LOGGER.exit();
	}

	/**
	 * 
	 * @return the name of the Virtue
	 */
	public String getName() {
		LOGGER.entry();
		LOGGER.exit(name);
		return name;
	}

	/**
	 * 
	 * @return the username to log in with for file access
	 */
	public String getUsername() {
		LOGGER.entry();
		LOGGER.exit(username);
		return username;
	}

	/**
	 * 
	 * @return the password to use for Samba access
	 */
	public String getPassword() {
		LOGGER.entry();
		LOGGER.exit(password);
		return password;
	}

	/**
	 * Set the password to the empty string. This is a security measure so we don't
	 * hold onto the password longer than necessary.
	 */
	public void clearPassword() {
		LOGGER.entry();
		password = "";
		LOGGER.exit();
	}
}
