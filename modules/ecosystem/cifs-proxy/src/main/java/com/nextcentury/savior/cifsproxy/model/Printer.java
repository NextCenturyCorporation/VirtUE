/**
 * 
 */
package com.nextcentury.savior.cifsproxy.model;

import java.util.Collection;

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
public class Printer implements Exportable {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(Printer.class);

	private String name;
	private String virtueId;
	private String server;
	private String exportedName;

	public Printer(@NonNull String name, @NonNull String virtueId, @NonNull String server) {
		super();
		LOGGER.entry(name, virtueId, server);
		this.name = name;
		this.virtueId = virtueId;
		this.server = server;
		LOGGER.exit();
	}

	public String getName() {
		LOGGER.entry();
		LOGGER.exit(name);
		return name;
	}

	public String getVirtueId() {
		LOGGER.entry();
		LOGGER.exit(virtueId);
		return virtueId;
	}

	public String getServer() {
		LOGGER.entry();
		LOGGER.exit(server);
		return server;
	}

	public String getExportedName() {
		LOGGER.entry();
		LOGGER.exit(exportedName);
		return exportedName;
	}

	public void initExportedName(Collection<? extends Printer> existingPrinters) throws IllegalStateException {
		LOGGER.entry(name);
		if (exportedName != null && exportedName.length() != 0) {
			IllegalStateException e = new IllegalStateException(
					"cannot init already-set exportedName '" + exportedName + "'");
			LOGGER.throwing(e);
			throw e;
		}
		exportedName = FileShare.createExportName(name, existingPrinters);
		LOGGER.exit();
	}

	@JsonIgnore
	public String getServiceName() {
		return "//" + server + "/" + name;
	}
}
