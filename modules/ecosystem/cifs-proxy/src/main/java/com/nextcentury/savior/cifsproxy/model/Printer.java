/**
 * 
 */
package com.nextcentury.savior.cifsproxy.model;

import org.springframework.lang.NonNull;

/**
 * Represents a network printer.
 * 
 * @author clong
 *
 */
public class Printer {
	private String name;
	private String virtueId;
	private String server;

	public Printer(@NonNull String name, @NonNull String virtueId, @NonNull String server) {
		super();
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public String getVirtueId() {
		return virtueId;
	}

	public String getServer() {
		return server;
	}
}
