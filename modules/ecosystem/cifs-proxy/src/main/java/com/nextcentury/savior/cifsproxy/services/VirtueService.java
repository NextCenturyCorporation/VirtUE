/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.stereotype.Service;

import com.nextcentury.savior.cifsproxy.model.Virtue;

/**
 * Provides operations on the set of Virtues the Proxy knows about.
 * 
 * @author clong
 *
 */
@Service
public class VirtueService {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(VirtueService.class);

	private static final long PROCESS_TIMEOUT_MS = 5000;

	protected Map<String, Virtue> virtuesByName = new HashMap<String, Virtue>();

	/**
	 * Define a new Virtue
	 * 
	 * @param virtue
	 *                   the new Virtue
	 * @throws IllegalArgumentException
	 *                                      if a Virtue with the same name was
	 *                                      already defined
	 * @throws IOException
	 *                                      if the Virtue could not be created
	 * @throws InterruptedIOException
	 *                                      if Virtue creation was interrupted
	 */
	public void newVirtue(Virtue virtue) throws IllegalArgumentException, InterruptedIOException, IOException {
		LOGGER.entry(virtue);
		if (virtuesByName.containsKey(virtue.getName())) {
			IllegalArgumentException e = new IllegalArgumentException(
					"virtue '" + virtue.getName() + "' already exists");
			throw e;
		}
		createLinuxUser(virtue);
		setSambaPassword(virtue);
		virtue.clearPassword();
		virtuesByName.put(virtue.getName(), virtue);
		LOGGER.exit();
	}

	private void createLinuxUser(Virtue virtue) throws IOException, InterruptedIOException {
		LOGGER.entry(virtue);
		ProcessBuilder processBuilder = new ProcessBuilder("sudo", "useradd", "--comment", virtue.getName(),
				"--no-create-home", "--no-user-group", "--shell", "/bin/false", virtue.getUsername());
		processBuilder.inheritIO();
		Process process = processBuilder.start();
		boolean processDone;
		try {
			processDone = process.waitFor(PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			InterruptedIOException ioe = new InterruptedIOException("useradd process was interrupted");
			ioe.initCause(e);
			LOGGER.throwing(ioe);
			throw ioe;
		}
		if (!processDone) {
			IOException ioe = new IOException("useradd took too long (> " + PROCESS_TIMEOUT_MS + "ms)");
			LOGGER.throwing(ioe);
			throw ioe;
		}
		int exitValue = process.exitValue();
		if (exitValue != 0) {
			IOException ioe = new IOException("useradd failed with code: " + exitValue);
			LOGGER.throwing(ioe);
			throw ioe;
		}
		LOGGER.exit();
	}

	private void setSambaPassword(Virtue virtue) throws IOException, InterruptedIOException {
		LOGGER.entry(virtue);
		int debuglevel = LOGGER.isTraceEnabled() ? 5 : LOGGER.isDebugEnabled() ? 2 : 0;
		String[] args = { "sudo", "smbpasswd", "-a", "-D", "" + debuglevel, "-s", virtue.getUsername() };
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("running smbpassword with: " + String.join(" ", args));
		}
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		processBuilder.redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT);
		Process process = processBuilder.start();
		OutputStream outputStream = process.getOutputStream();
		OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
		outputWriter.write(virtue.getPassword());
		outputWriter.write('\n');
		outputWriter.write(virtue.getPassword());
		outputWriter.write('\n');
		outputWriter.flush();

		boolean processDone;
		try {
			processDone = process.waitFor(PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			InterruptedIOException ioe = new InterruptedIOException("smbpasswd process was interrupted");
			ioe.initCause(e);
			LOGGER.throwing(ioe);
			throw ioe;
		}
		if (!processDone) {
			IOException ioe = new IOException("smbpasswd took too long (> " + PROCESS_TIMEOUT_MS + "ms)");
			LOGGER.throwing(ioe);
			throw ioe;
		}
		int exitValue = process.exitValue();
		if (exitValue != 0) {
			IOException ioe = new IOException("smbpasswd failed with code: " + exitValue);
			LOGGER.throwing(ioe);
			throw ioe;
		}
		LOGGER.exit();
	}
}
