/**
 * 
 */
package com.nextcentury.savior.cifsproxy.controllers;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nextcentury.savior.cifsproxy.model.Printer;
import com.nextcentury.savior.cifsproxy.services.PrinterService;

/**
 * Implements the CIFS Proxy REST API for printers as defined in the SAVIOR CIFS
 * Proxy documentation.
 * 
 * @author clong
 *
 */
public class PrinterController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(PrinterController.class);

	@Autowired
	private PrinterService service;
	
	@GetMapping("printer")
	Collection<Printer> getPrinters() {
		LOGGER.entry();
		Collection<Printer> printers = service.getPrinters();
		LOGGER.exit(printers);
		return printers;
	}
	
	@GetMapping("/share/{name}")
	Printer getPrinter(@PathVariable String name) {
		LOGGER.entry(name);
		Printer printer = service.getPrinter(name);
		LOGGER.exit(printer);
		return printer;
	}
	
	@PostMapping("/printer")
	Printer newPrinter(HttpSession session, @RequestBody Printer printer) {
		LOGGER.entry(session, printer);
		Printer newPrinter;
		try {
			newPrinter = service.newPrinter(session, printer);
		} catch (IllegalArgumentException e) {
			WebServerException wse = new WebServerException("exception mounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(newPrinter);
		return newPrinter;
	}

	@DeleteMapping("/printer/{name}")
	void removeShare(@PathVariable String name) {
		LOGGER.entry();
		try {
			service.removePrinter(name);
		} catch (IllegalArgumentException e) {
			WebServerException wse = new WebServerException("exception unmounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}	
}
