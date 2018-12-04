package com.nextcentury.savior.cifsproxy.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

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

import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.services.ShareService;

/**
 * Implements the CIFS Proxy REST API as defined in the SAVIOR CIFS Proxy
 * documentation.
 * 
 * @author clong
 *
 */
@RestController
public class ShareController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(ShareController.class);

	@Autowired
	private ShareService service;

	@GetMapping("/share")
	List<FileShare> getShares() {
		LOGGER.entry();
		ArrayList<FileShare> shares = new ArrayList<>(service.getShares());
		LOGGER.exit(shares);
		return shares;
	}

	@GetMapping("/share/{name}")
	FileShare getShare(@PathVariable String name) {
		LOGGER.entry(name);
		FileShare share = service.getShare(name);
		LOGGER.exit(share);
		return share;
	}

	@PostMapping("/share")
	FileShare newShare(HttpSession session, @RequestBody FileShare share) {
		LOGGER.entry(session, share);
		try {
			service.newShare(session, share);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception mounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(share);
		return share;
	}

	@DeleteMapping("/share/{name}")
	void removeShare(@PathVariable String name) {
		LOGGER.entry();
		try {
			service.removeShare(name);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception unmounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}

	@GetMapping("/scan")
	String scan() {
		LOGGER.entry();
		try {
			service.scan();
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException(e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit("");
		return "";
	}
}
