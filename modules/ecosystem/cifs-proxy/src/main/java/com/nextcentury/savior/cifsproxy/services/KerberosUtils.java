/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import com.nextcentury.savior.cifsproxy.DelegatingAuthenticationManager;
import com.nextcentury.savior.cifsproxy.GssApi;

/**
 * Utility methods for dealing with Kerberos tickets.
 * 
 * @author clong
 *
 */
class KerberosUtils {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(KerberosUtils.class);

	/**
	 * Initialize a credential cache with our service credential. Currently uses
	 * <code>kinit</code> but ideally we'd do this with {@link GssApi} calls.
	 * 
	 * @param cCacheFilename
	 *                           the file to initialize
	 * @param keytabFilename
	 *                           the keytab to initialize with. Must contain the key
	 *                           for the service (see
	 *                           {@link DelegatingAuthenticationManager#getServiceName(char)})
	 * @param domain
	 *                           the Kerberos/Active Directory domain for the
	 *                           service
	 */
	static void initCCache(String cCacheFilename, String keytabFilename, String domain) {
		LOGGER.entry(cCacheFilename, keytabFilename);
		String serviceName = DelegatingAuthenticationManager.getServiceName('/') + "." + domain;
		ProcessBuilder processBuilder = createProcessBuilder(cCacheFilename);
		processBuilder.command("kinit", "-k", "-t", keytabFilename, serviceName);
		runProcess(processBuilder, "kinit");
		extraTracing(cCacheFilename);
		LOGGER.exit();
	}

	/**
	 * Import credentials from one credentials file to another. Does not affect the
	 * principal. Currently uses a helper program (importcreds), but ideally would
	 * use {@link GssApi}.
	 * 
	 * @param toCCacheFilename
	 *                               destination credential file
	 * @param fromCCacheFilename
	 *                               source credential file
	 */
	static void importCredentials(String toCCacheFilename, String fromCCacheFilename) {
		LOGGER.entry(toCCacheFilename, fromCCacheFilename);
		ProcessBuilder processBuilder = createProcessBuilder(toCCacheFilename);
		processBuilder.command("importcreds", fromCCacheFilename);
		runProcess(processBuilder, "importcreds");
		extraTracing(toCCacheFilename);
		LOGGER.exit();
	}

	/**
	 * Get a proxy service ticket for a CIFS server and a specific user. Currently
	 * uses <code>kvno</code>, but ideally would use {@link GssApi}.
	 * 
	 * @param ccacheFilename
	 *                           location of existing credentials
	 * @param username
	 *                           user to get creds for
	 * @param server
	 *                           file server we need creds for
	 * @param keytabFilename
	 *                           location of our private creds
	 */
	static void getServiceTicket(String ccacheFilename, String username, String server, String keytabFilename) {
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

	/**
	 * We obtain all the credentials with us (http/cifsProxyserver...) as the
	 * principal and in a file, but mount.cifs(8) needs the user to be the principal
	 * and in the default credential cache of some (local) user (not some arbitrary
	 * file like we've been using up to this point in the process). This method
	 * switches principals and copies the credential into the default credential
	 * cache for <code>mountUser</code>. Currently uses a helper program
	 * (<code>switchprincipal</code>), but ideally would use {@link GssApi}.
	 * 
	 * @param ccacheFilename
	 *                           where our credentials are
	 * @param mountUser
	 *                           the user to copy credentials to (specifically to
	 *                           this user's default cache)
	 * @param username
	 *                           the new principal (the user whose credentials will
	 *                           be used to mount the files)
	 */
	static void switchPrincipal(String ccacheFilename, String mountUser, String username) {
		LOGGER.entry(ccacheFilename, mountUser, username);
		String simpleUsername = username.split("@")[0];
		ProcessBuilder processBuilder = createProcessBuilder(null);
		processBuilder.command("sudo", "-u", mountUser, "switchprincipal", "-i", ccacheFilename, simpleUsername);
		runProcess(processBuilder, "switchprincipal");
		LOGGER.exit();
	}

	/**
	 * Helper function to debugging, by printing contents of a credential cache.
	 * 
	 * @param ccacheFile
	 *                       cache file to print
	 */
	private static void extraTracing(String ccacheFile) {
		// this is a (short) debugging function, so don't trace entry/exit
		if (LOGGER.isTraceEnabled()) {
			runProcess(createProcessBuilder(ccacheFile).command("klist"), "klist");
		}
	}

	/**
	 * Makes it easy to launch a subprocess that does Kerberos things.
	 * 
	 * @param ccacheFilename
	 *                           the Kerberos credential cache to use (or
	 *                           <code>null</code> to use the default)
	 * @return the new process builder
	 * @see KerberosUtils#KERBEROS_CCACHE_ENV_VAR
	 */
	static ProcessBuilder createProcessBuilder(String ccacheFilename) {
		LOGGER.entry(ccacheFilename);
		ProcessBuilder pb = new ProcessBuilder();
		Map<String, String> environment = pb.environment();
		if (ccacheFilename != null) {
			environment.put(KerberosUtils.KERBEROS_CCACHE_ENV_VAR, "FILE:" + ccacheFilename);
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(KerberosUtils.KERBEROS_CCACHE_ENV_VAR + "=" + environment.get(KerberosUtils.KERBEROS_CCACHE_ENV_VAR));
			if (!environment.containsKey("KRB5_TRACE")) {
				environment.put("KRB5_TRACE", "/dev/stdout");
			}
		}
	
		LOGGER.exit(pb);
		return pb;
	}

	/**
	 * Run a process that's expected to complete successfully, and wait a finite
	 * amount of time ({@link KerberosUtils#MOUNT_TIMEOUT}) for it.
	 * 
	 * @param processBuilder
	 *                           what to run
	 * @param name
	 *                           name of the process (used for debugging & error
	 *                           messages)
	 */
	static void runProcess(ProcessBuilder processBuilder, String name) {
		LOGGER.entry(processBuilder, name);
		processBuilder.inheritIO();
		Process process;
		if (LOGGER.isDebugEnabled()) {
			StringBuilder envString = new StringBuilder();
			Map<String, String> environment = processBuilder.environment();
			if (environment.containsKey(KerberosUtils.KERBEROS_CCACHE_ENV_VAR)) {
				envString.append("env ");
				envString.append(KerberosUtils.KERBEROS_CCACHE_ENV_VAR);
				envString.append('=');
				envString.append(environment.get(KerberosUtils.KERBEROS_CCACHE_ENV_VAR));
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
			process.waitFor(KerberosUtils.MOUNT_TIMEOUT, TimeUnit.SECONDS);
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
	 * Time until we assume mount has failed (in seconds)
	 */
	static final long MOUNT_TIMEOUT = 60;
	/**
	 * The magic Kerberos environment variable telling it which credentials cache to
	 * use (e.g., which file)
	 */
	static final String KERBEROS_CCACHE_ENV_VAR = "KRB5CCNAME";

}
