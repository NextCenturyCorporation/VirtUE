package com.nextcentury.savior.cifsproxy.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.ActiveDirectorySecurityConfig;
import com.nextcentury.savior.cifsproxy.BaseSecurityConfig;
import com.nextcentury.savior.cifsproxy.DelegatingAuthenticationManager;

@RestController
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class MountController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(MountController.class);

	/** path to the mount command */
	private static final String MOUNT_COMMAND = "/bin/mount";

	private static final long MOUNT_TIMEOUT = 60;

	private static final String KERBEROS_CCACHE_ENV_VAR = "KRB5CCNAME";

	private static final Object MOUNT_CREDENTIALS_LOCK = new Object();

	/** where to mount files for the Virtue */
	@Value("${mountRoot:/mnt/cifs-proxy}")
	public String MOUNT_ROOT;

	/**
	 * The user whose credentials will be set and used to do the mount (because
	 * mount.cifs(8) can only use default user credentials and ignores
	 * {@value #KERBEROS_CCACHE_ENV_VAR}.
	 */
	@Value("${mountUser:mounter}")
	public String mountUser;

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

		String username = (String) session.getAttribute(ActiveDirectorySecurityConfig.USERNAME_ATTRIBUTE);
		Path ccachePath = (Path) session.getAttribute(ActiveDirectorySecurityConfig.CCACHE_PATH_ATTRIBUTE);
		String ccacheFilename = ccachePath.toAbsolutePath().toString();
		Path keytabPath = (Path) session.getAttribute(ActiveDirectorySecurityConfig.KEYTAB_PATH_ATTRIBUTE);
		String keytabFilename = keytabPath.toAbsolutePath().toString();

		Path intermediateCCache = null;
		try {
			intermediateCCache = Files.createTempFile("cifsproxy-intermediate-ccache", "");
			String intermediateCCacheFilename = intermediateCCache.toAbsolutePath().toString();
			/*
			 * Modeled after the manual process (for now)
			 */
			initCCache(intermediateCCacheFilename, keytabFilename);
			importCredentials(intermediateCCacheFilename, ccacheFilename);
			getServiceTicket(intermediateCCacheFilename, username, server, keytabFilename);
			intermediateCCache.toFile().setReadable(true, false);
			// only one at a time can muck with default user credentials (of mountUser)
			synchronized (MOUNT_CREDENTIALS_LOCK) {
				switchPrincipal(intermediateCCacheFilename, mountUser, username);
				doMount(server, sourcePath, mountPath, readOnly, username);
			}
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException("could not create temporary file", e);
			LOGGER.throwing(wse);
			throw wse;
		} finally {
			if (intermediateCCache != null && !LOGGER.isDebugEnabled()) {
				LOGGER.trace("deleting temporary mount file: " + intermediateCCache);
				intermediateCCache.toFile().delete();
			}
		}

		LOGGER.exit("ok");
		return "ok";
	}

	private void initCCache(String cCacheFilename, String keytabFilename) {
		LOGGER.entry(cCacheFilename, keytabFilename);
		String serviceName = DelegatingAuthenticationManager.getServiceName('/');
		ProcessBuilder processBuilder = createProcessBuilder(cCacheFilename);
		processBuilder.command("kinit", "-k", "-t", keytabFilename, serviceName);
		runProcess(processBuilder, "kinit");
		extraTracing(cCacheFilename);
		LOGGER.exit();
	}

	private void importCredentials(String toCCacheFilename, String fromCCacheFilename) {
		LOGGER.entry(toCCacheFilename, fromCCacheFilename);
		ProcessBuilder processBuilder = createProcessBuilder(toCCacheFilename);
		processBuilder.command("importcreds", fromCCacheFilename);
		runProcess(processBuilder, "importcreds");
		extraTracing(toCCacheFilename);
		LOGGER.exit();
	}

	private void getServiceTicket(String ccacheFilename, String username, String server, String keytabFilename) {
		LOGGER.entry(ccacheFilename, username, server, keytabFilename);
		// example: kvno -k /etc/krb5.keytab -P -U bob cifs/fileserver.test.savior
		ProcessBuilder processBuilder = createProcessBuilder(ccacheFilename);
		String service = "cifs/" + server;
		String simpleUsername = username.split("@")[0];
		processBuilder.command("kvno", "-k", keytabFilename, "-P", "-U", simpleUsername, service);
		runProcess(processBuilder, "kvno");
		extraTracing(ccacheFilename);
		LOGGER.exit();
	}

	private void switchPrincipal(String ccacheFilename, String mountUser, String username) {
		LOGGER.entry(ccacheFilename, mountUser, username);
		String simpleUsername = username.split("@")[0];
		ProcessBuilder processBuilder = createProcessBuilder(null);
		processBuilder.command("sudo", "-u", mountUser, "switchprincipal", "-i", ccacheFilename, simpleUsername);
		runProcess(processBuilder, "switchprincipal");
		LOGGER.exit();
	}

	private void extraTracing(String ccacheFile) {
		// this is a (short) debugging function, so don't trace entry/exit
		if (LOGGER.isTraceEnabled()) {
			runProcess(createProcessBuilder(ccacheFile).command("klist"), "klist");
		}
	}

	private ProcessBuilder createProcessBuilder(String ccacheFilename) {
		LOGGER.entry(ccacheFilename);
		ProcessBuilder pb = new ProcessBuilder();
		Map<String, String> environment = pb.environment();
		if (ccacheFilename != null) {
			environment.put(KERBEROS_CCACHE_ENV_VAR, "FILE:" + ccacheFilename);
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(KERBEROS_CCACHE_ENV_VAR + "=" + environment.get(KERBEROS_CCACHE_ENV_VAR));
			if (!environment.containsKey("KRB5_TRACE")) {
				environment.put("KRB5_TRACE", "/dev/stdout");
			}
		}

		LOGGER.exit(pb);
		return pb;
	}

	/**
	 * Run a process that's expected to complete successfully, and wait a finite
	 * amount of time ({@link #MOUNT_TIMEOUT}) for it.
	 * 
	 * @param processBuilder
	 *                           what to run
	 * @param name
	 *                           name of the process (used for debugging & error
	 *                           messages)
	 */
	private void runProcess(ProcessBuilder processBuilder, String name) {
		LOGGER.entry(processBuilder, name);
		processBuilder.inheritIO();
		Process process;
		if (LOGGER.isDebugEnabled()) {
			StringBuilder envString = new StringBuilder();
			Map<String, String> environment = processBuilder.environment();
			if (environment.containsKey(KERBEROS_CCACHE_ENV_VAR)) {
				envString.append("env ");
				envString.append(KERBEROS_CCACHE_ENV_VAR);
				envString.append('=');
				envString.append(environment.get(KERBEROS_CCACHE_ENV_VAR));
				envString.append(' ');
			}
			LOGGER.debug("Starting process: " + name + ": " + envString + String.join(" ", processBuilder.command()));
		}
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException("failed to start " + name + " process", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		try {
			process.waitFor(MOUNT_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			WebServiceException wse = new WebServiceException("interrupted while waiting for " + name + " process", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		int exitValue = process.exitValue();
		LOGGER.trace("returned from " + name + " with: " + exitValue);
		if (exitValue != 0) {
			WebServiceException wse = new WebServiceException("error result from process " + name + ": " + exitValue);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}

	/**
	 * Carry out the mount operation
	 * 
	 * @param server
	 *                       remote server to mount files from
	 * @param sourcePath
	 *                       path on the remote server
	 * @param mountPath
	 *                       local path to mount onto
	 * @param readOnly
	 *                       whether it should be read only (or read-write if
	 *                       <code>false</code>)
	 * @param username
	 *                       the user whose credentials we should use
	 * @throws WebServiceException
	 */
	private void doMount(String server, String sourcePath, String mountPath, boolean readOnly, String username)
			throws WebServiceException {
		LOGGER.entry(server, sourcePath, mountPath, readOnly, username);
		File destination = new File(MOUNT_ROOT, mountPath);
		// make sure the destination is inside MOUNT_ROOT
		String canonicalDest;
		try {
			canonicalDest = destination.getCanonicalPath();
			LOGGER.debug("canonical mount path: " + canonicalDest);
			if (!canonicalDest.substring(0, MOUNT_ROOT.length()).equals(MOUNT_ROOT)
					|| canonicalDest.charAt(MOUNT_ROOT.length()) != File.separatorChar) {
				WebServiceException wse = new WebServiceException("invalid mount path: " + mountPath);
				LOGGER.throwing(wse);
				throw wse;
			}
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException("could not create full path for mount path: " + mountPath,
					e);
			LOGGER.throwing(wse);
			throw wse;
		}
		if (!destination.exists() && !destination.mkdir()) {
			WebServiceException wse = new WebServiceException("could not create target mountpoint: " + mountPath);
			LOGGER.throwing(wse);
			throw wse;
		}
		// assemble arguments for mounting cifs
		if (!sourcePath.startsWith("/")) {
			sourcePath = "/" + sourcePath;
		}
		String simpleUsername = username.split("@")[0];

		String options = "username=" + simpleUsername + (readOnly ? ",ro" : "") + ",sec=krb5,cruid=" + mountUser;
		String[] args = { "sudo", MOUNT_COMMAND, "-t", "cifs", "//" + server + sourcePath,
				destination.getAbsolutePath(), "-v", "-o", options };
		LOGGER.debug("mount command: " + Arrays.toString(args));
		ProcessBuilder processBuilder = createProcessBuilder(null);
		processBuilder.command(args);
		runProcess(processBuilder, "mount");
		LOGGER.exit();
	}

	@RequestMapping(path = "/unmount", params = { "mountPath" }, produces = "application/json", method = {
			RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String unmountDirectory(HttpSession session, @RequestParam("mountPath") String mountPath) {

		return "not implemented yet";
	}

	private void doUnMount(String mountPath, boolean wait) {
		// TODO Auto-generated method stub

	}
}
