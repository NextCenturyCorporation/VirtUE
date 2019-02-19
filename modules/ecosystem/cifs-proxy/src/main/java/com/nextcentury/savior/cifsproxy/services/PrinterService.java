/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.nextcentury.savior.cifsproxy.model.Printer;

/**
 * @author clong
 *
 */
public class PrinterService {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(PrinterService.class);

	private Map<String, Printer> printers;

	public Collection<Printer> getPrinters() {
		LOGGER.entry();
		LOGGER.exit(printers.values());
		return printers.values();
	}

	public Printer getPrinter(String name) {
		LOGGER.entry(name);
		Printer printer = printers.get(name);
		LOGGER.exit(printer);
		return printer;
	}

	public Printer newPrinter(HttpSession session, Printer printer) throws IllegalArgumentException {
		if (printer.getName() == null || printer.getName().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("name cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (printer.getVirtueId() == null || printer.getVirtueId().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("virtueId cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (printer.getServer() == null || printer.getServer().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("server cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		
		// TODO
		return printer;
	}

	public void removePrinter(String name) throws IllegalArgumentException {
		LOGGER.entry(name);
		// TODO
		LOGGER.exit();
	}

}
