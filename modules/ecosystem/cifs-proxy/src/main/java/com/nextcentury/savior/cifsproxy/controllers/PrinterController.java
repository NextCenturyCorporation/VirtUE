/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
/**
 * 
 */
package com.nextcentury.savior.cifsproxy.controllers;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RestController;

import com.ncc.savior.virtueadmin.template.ITemplateService.TemplateException;
import com.nextcentury.savior.cifsproxy.model.Printer;
import com.nextcentury.savior.cifsproxy.services.PrinterService;

/**
 * Implements the CIFS Proxy REST API for printers as defined in the SAVIOR CIFS
 * Proxy documentation.
 * 
 * @author clong
 *
 */
@RestController
public class PrinterController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(PrinterController.class);

	@Autowired
	private PrinterService service;
	
	@GetMapping("/printer")
	Collection<Printer> getPrinters() {
		LOGGER.entry();
		Collection<Printer> printers = service.getPrinters();
		LOGGER.exit(printers);
		return printers;
	}
	
	@GetMapping("/printer/{name}")
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
		} catch (IllegalArgumentException | TemplateException | IOException e) {
			WebServerException wse = new WebServerException("exception adding a printer", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(newPrinter);
		return newPrinter;
	}

	@DeleteMapping("/printer/{name}")
	void removePrinter(@PathVariable String name) {
		LOGGER.entry();
		try {
			service.removePrinter(name);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception removing a printer", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}	
}
