package com.nextcentury.savior.cifsproxy.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.ActiveDirectorySecurityConfig;

@RestController
public class MountController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(MountController.class);

	/** path to the mount command */
	private static final String MOUNT_COMMAND = "/bin/mount";

	private static final long MOUNT_TIMEOUT = 60;

	/** where to mount files for the Virtue */
	public final String MOUNT_ROOT = "/mnt/cifs-proxy";

	@RequestMapping(path = "/mount", params = { "server", "sourcePath", "permissions",
			"mountPath" }, produces = "application/json", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String mountDirectory(HttpSession session, @RequestParam("server") String server,
			@RequestParam("sourcePath") String sourcePath,
			@RequestParam(value = "permissions", defaultValue = "rw") String permissions,
			@RequestParam("mountPath") String mountPath) {
		LOGGER.entry(session, server, sourcePath, permissions, mountPath);
		boolean readOnly;
		switch (permissions) {
		case "rw":
			readOnly = false;
			break;
		case "r":
			readOnly = true;
			break;
		default:
			throw new IllegalArgumentException("permissions must be 'r' or 'rw' (was '" + permissions + "')");
		}

		File mountRoot = new File(MOUNT_ROOT);
		if (!mountRoot.exists()) {
			LOGGER.trace("making directory: " + MOUNT_ROOT);
			mountRoot.mkdirs();
			if (!mountRoot.exists()) {
				WebServiceException exception = new WebServiceException(
						"could not create mount directory: " + MOUNT_ROOT);
				LOGGER.throwing(exception);
				throw exception;
			}
		}

		// assemble arguments for mounting cifs
		if (!sourcePath.startsWith("/")) {
			sourcePath = "/" + sourcePath;
		}
		String username = (String) session.getAttribute(ActiveDirectorySecurityConfig.USERNAME_ATTRIBUTE);
		String options = "username=" + username + (readOnly ? ",ro" : "") + ",sec=krb5";
		File destination = new File(MOUNT_ROOT, mountPath);
		String[] args = { MOUNT_COMMAND, "-t", "cifs", "//" + server + sourcePath, destination.getAbsolutePath(), "-o",
				options };
		LOGGER.debug("mount command: " + Arrays.toString(args));
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		LOGGER.debug("setting kerberos cache file to: " + ActiveDirectorySecurityConfig.serviceTicketFile);
		processBuilder.environment().put("KRB5CCNAME", ActiveDirectorySecurityConfig.serviceTicketFile.toString());
		Process process;
		int retval;
		try {
			LOGGER.trace("starting mount command");
			long mountStartTime = System.currentTimeMillis();
			process = processBuilder.start();
			LOGGER.trace("waiting for mount command...");
			if (!process.waitFor(MOUNT_TIMEOUT, TimeUnit.SECONDS)) {
				process.destroy();
				doUnMount(mountPath, false);
				WebServiceException exception = new WebServiceException(
						"mount took longer than " + MOUNT_TIMEOUT + " seconds");
				LOGGER.throwing(exception);
				throw exception;
			}
			retval = process.exitValue();
			long mountEndTime = System.currentTimeMillis();
			LOGGER.trace("mount exited: " + retval + "(after " + (mountEndTime - mountStartTime) + " ms)");
		} catch (IOException | InterruptedException e) {
			WebServiceException exception = new WebServiceException("error running mount process", e);
			LOGGER.throwing(exception);
			throw exception;
		}
		if (retval != 0) {
			WebServiceException exception = new WebServiceException("mount failed, error: " + retval);
			LOGGER.throwing(exception);
			throw exception;
		}

		LOGGER.exit("ok");
		return "ok";
	}

	private void doUnMount(String mountPath, boolean wait) {
		// TODO Auto-generated method stub

	}
}
