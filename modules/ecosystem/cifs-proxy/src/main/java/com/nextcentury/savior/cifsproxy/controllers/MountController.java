package com.nextcentury.savior.cifsproxy.controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.DelegatingAuthenticationManager;
import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.model.FileShare.SharePermissions;
import com.nextcentury.savior.cifsproxy.model.FileShare.ShareType;
import com.nextcentury.savior.cifsproxy.services.ShareService;

/**
 * Handles the REST API for mounting and unmounting Windows shares.
 * 
 * @author clong
 *
 * @see DelegatingAuthenticationManager
 */
@RestController
public class MountController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(MountController.class);

	@Autowired
	private ShareService service;

	@RequestMapping(path = "/mount", params = { "virtue", "server", "sourcePath", "permissions",
			"mountPath" }, produces = "application/json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public FileShare mountDirectory(HttpSession session, @RequestParam("virtue") String virtue,
			@RequestParam("server") String server, @RequestParam("sourcePath") String sourcePath,
			@RequestParam(value = "permissions", required = false, defaultValue = "rw") String permissions,
			@RequestParam("mountPath") String mountPath) {
		LOGGER.entry(session, server, sourcePath, permissions, mountPath);
		Set<SharePermissions> permissionSet = new HashSet<>();
		switch (permissions) {
		case "rw":
			permissionSet.add(SharePermissions.WRITE);
			// fallthrough
		case "r":
			permissionSet.add(SharePermissions.READ);
			break;
		default:
			throw new IllegalArgumentException("permissions must be 'r' or 'rw' (was '" + permissions + "')");
		}

		FileShare fileShare = new FileShare(virtue + "_" + server + sourcePath.replace(File.pathSeparatorChar, '_'),
				virtue, server, sourcePath, permissionSet, ShareType.CIFS);
		try {
			service.newShare(session, fileShare);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception creating a new share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(fileShare);
		return fileShare;
	}

	@RequestMapping(path = "/unmount", params = { "mountPath" }, produces = "application/json", method = {
			RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String unmountDirectory(HttpSession session, @RequestParam("mountPath") String mountPath) {

		return "not implemented yet";
	}
}
