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
	private String id;
	private String username;
	private String password;

	/**
	 * 
	 * @param name
	 *                     the name of the Virtue
	 * @param id
	 *                     the unique identifier for the Virtue
	 */
	public Virtue(String name, String id) {
		super();
		LOGGER.entry(name, id);
		this.name = name;
		this.id = id;
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
	 * 
	 * @return the unique identifier for the Virtue
	 */
	public String getId() {
		LOGGER.entry();
		LOGGER.exit(id);
		return id;
	}

	public void initUsername(String username) {
		LOGGER.entry(username);
		if (this.username != null && this.username.length() > 0) {
			IllegalStateException e = new IllegalStateException(
					"cannot initialize an already-set username '" + this.username + "'");
			LOGGER.throwing(e);
			throw e;
		}
		this.username = username;
		LOGGER.exit();
	}

	public void initPassword(String password) {
		LOGGER.entry(password);
		if (this.password != null && this.password.length() > 0) {
			IllegalStateException e = new IllegalStateException(
					"cannot initialize an already-set password '" + this.password + "'");
			LOGGER.throwing(e);
			throw e;
		}
		this.password = password;
		LOGGER.exit();
	}
}
