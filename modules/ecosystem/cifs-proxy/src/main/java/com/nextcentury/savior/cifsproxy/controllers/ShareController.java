package com.nextcentury.savior.cifsproxy.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.services.ShareService;

/**
 * Implements the CIFS Proxy REST API as defined in the SAVIOR CIFS Proxy documentation.
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
		
	@PostMapping("/share")
	FileShare newShare(HttpSession session, @RequestBody FileShare share) {
		LOGGER.entry(session, share);
		validateShare(share);
		service.newShare(session, share);
		LOGGER.exit(share);
		return share;
	}

	private void validateShare(FileShare share) {
		if (share.getName() == null || share.getName().isEmpty()) {
			throw new IllegalArgumentException("");
		}
	}
	
}
