/**
 * 
 */
package com.nextcentury.savior.cifsproxy.model;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a network printer.
 * 
 * @author clong
 *
 */
public class Printer extends SambaService {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(Printer.class);

	public Printer(@NonNull String name, @NonNull String virtueId, @NonNull String server) {
		super();
		LOGGER.entry(name, virtueId, server);
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
		LOGGER.exit();
	}

	@JsonIgnore
	public String getServiceName() {
		return "//" + server + "/" + name;
	}
}
