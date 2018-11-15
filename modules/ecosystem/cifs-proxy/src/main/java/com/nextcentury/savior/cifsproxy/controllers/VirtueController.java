package com.nextcentury.savior.cifsproxy.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.model.Virtue;
import com.nextcentury.savior.cifsproxy.services.VirtueService;

@RestController
public class VirtueController {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(VirtueController.class);

	@Autowired
	private VirtueService service;

	@PostMapping("/virtue")
	Virtue newVirtue(@RequestBody Virtue virtue) {
		LOGGER.entry(virtue);
		try {
			service.newVirtue(virtue);
		} catch (IllegalArgumentException | IOException e) {
			WebServiceException wse = new WebServiceException("exception while creating a new Virtue", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(virtue);
		return virtue;
	}
	
	@GetMapping("/virtue")
	List<Virtue> getVirtues() {
		LOGGER.entry();
		ArrayList<Virtue> virtues = new ArrayList<>(service.getVirtues());
		LOGGER.exit(virtues);
		return virtues;
	}

	@GetMapping("/virtue/{name}")
	Virtue getVirtue(@PathVariable String name) {
		LOGGER.entry(name);
		Virtue virtue = service.getVirtue(name);
		LOGGER.exit(virtue);
		return virtue;
	}	
}
