/**
 * 
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

	/**
	 * Maximum length of a Linux username, including null terminator.
	 */
	private static final int MAX_USERNAME_LENGTH = 32;

	private static final String PASSWORD_FILE = "/etc/passwd";

	protected Map<String, Virtue> virtuesById = new HashMap<String, Virtue>();

	/**
	 * Define a new Virtue
	 * 
	 * @param virtue
	 *                   the new Virtue
	 * @throws IllegalArgumentException
	 *                                      if the passed Virtue has invalid fields
	 *                                      (e.g., Virtue with the same ID was
	 *                                      already defined)
	 * @throws IOException
	 *                                      if the Virtue could not be created
	 * @throws InterruptedIOException
	 *                                      if Virtue creation was interrupted
	 */
	public void newVirtue(Virtue virtue) throws IllegalArgumentException, InterruptedIOException, IOException {
		LOGGER.entry(virtue);
		if (virtuesById.containsKey(virtue.getId())) {
			IllegalArgumentException e = new IllegalArgumentException("virtue '" + virtue.getId() + "' already exists");
			LOGGER.throwing(e);
			throw e;
		}
		if (virtue.getUsername() == null || virtue.getUsername().length() == 0) {
			virtue.initUsername(createUsername(virtue));
		} else {
			Set<String> allUsers = getAllUsers();
			if (allUsers.contains(virtue.getUsername())) {
				IllegalArgumentException e = new IllegalArgumentException(
						"duplicate user '" + virtue.getUsername() + "'");
				LOGGER.throwing(e);
				throw e;
			}
		}
		if (virtue.getPassword() == null || virtue.getPassword().length() == 0) {
			virtue.initPassword(createPassword());
		}
		createLinuxUser(virtue);
		setSambaPassword(virtue);
		virtuesById.put(virtue.getId(), virtue);
		LOGGER.exit();
	}

	private String createPassword() {
		LOGGER.entry();
		Random random = new Random();
		int length = 12 + random.nextInt(4);
		String password = new Random().ints(length, 33, 122)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
		LOGGER.exit(password);
		return password;
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
		switch (exitValue) {
		case 0: // OK
			break;
		case 9: // username already in use
			LOGGER.info("virtue '" + virtue.getUsername() + "' already existed");
			break;
		default:
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
		LOGGER.trace("starting smbpassword");
		Process process = processBuilder.start();
		OutputStream outputStream = process.getOutputStream();
		OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
		LOGGER.trace("sending password to smbpassword");
		outputWriter.write(virtue.getPassword());
		outputWriter.write('\n');
		outputWriter.write(virtue.getPassword());
		outputWriter.write('\n');
		outputWriter.flush();

		LOGGER.trace("waiting for smbpassword...");
		boolean processDone;
		try {
			processDone = process.waitFor(PROCESS_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			InterruptedIOException ioe = new InterruptedIOException("smbpasswd process was interrupted");
			ioe.initCause(e);
			LOGGER.throwing(ioe);
			throw ioe;
		}
		LOGGER.trace("...done waiting for smbpassword");
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

	public Collection<Virtue> getVirtues() {
		LOGGER.entry();
		Collection<Virtue> virtues = virtuesById.values();
		LOGGER.exit(virtues);
		return virtues;
	}

	public Virtue getVirtue(String id) {
		LOGGER.entry(id);
		Virtue virtue = virtuesById.get(id);
		LOGGER.exit(virtue);
		return virtue;
	}

	/**
	 * Create a Virtue username from its name, subject to Linux username
	 * constraints, and such that it does not collide with any already-known
	 * Virtues.
	 * 
	 * POSIX allows any name with characters in the set [a-zA-Z0-9._-] except that
	 * the first character cannot be '-' (see
	 * http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap03.html#tag_03_437
	 * and
	 * http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap03.html#tag_03_282).
	 * However, some Linux systems require usernames to match "^[a-z][-a-z0-9_.]*$"
	 * and be at most {@link #MAX_USERNAME_LENGTH} characters long (possibly
	 * including the null terminator), so this method creates names that comply with
	 * that.
	 * 
	 * @return
	 * @throws IOException
	 *                                   if the password file could not be read
	 * @throws FileNotFoundException
	 *                                   if the password file does not exist
	 */
	public String createUsername(Virtue virtue) throws FileNotFoundException, IOException {
		StringBuilder username = new StringBuilder();
		// create a candidate username
		String virtueName = virtue.getName();
		int maxLen = Math.min(virtueName.length(), MAX_USERNAME_LENGTH - 1);
		for (int i = 0; i < maxLen; i++) {
			char c = virtueName.charAt(i);
			char newChar;
			switch (Character.getType(c)) {
			case Character.LOWERCASE_LETTER:
				newChar = c;
				break;
			case Character.UPPERCASE_LETTER:
				newChar = Character.toLowerCase(c);
				break;
			case Character.DECIMAL_DIGIT_NUMBER:
				if (username.length() > 0) {
					newChar = c;
				} else {
					continue;
				}
				break;
			default:
				if (username.length() > 0) {
					switch (c) {
					case '-':
					case '_':
					case '.':
						newChar = c;
						break;
					default:
						// invalid char for username
						newChar = '_';
						break;
					}
				} else {
					continue;
				}
			}
			username.append(newChar);
		}
		if (username.length() == 0) {
			username.append("virtue");
		}

		Set<String> allUsers = getAllUsers();
		// make sure it's unique
		Collection<Virtue> virtues = virtuesById.values();
		int suffix = 1;
		while (virtues.stream().anyMatch((Virtue v) -> v.getUsername().equals(username.toString()))
				|| allUsers.contains(username.toString())) {
			suffix++;
			String suffixAsString = Integer.toString(suffix);
			// replace the end with the suffix
			int baseLength = Math.min(username.length(), MAX_USERNAME_LENGTH - 1 - suffixAsString.length());
			username.replace(baseLength, username.length(), suffixAsString);
		}
		return username.toString();
	}

	/**
	 * Figure out currently valid user names. Since the Proxy will only allow local
	 * user logins, we can just read /etc/passwd. If the Proxy allowed domain
	 * logins, we'd need to run getent(1), instead.
	 * 
	 * @return
	 * @throws IOException
	 *                                   if there was an error reading the password
	 *                                   file (see {@link #createPassword()})
	 * @throws FileNotFoundException
	 *                                   if the {@link #PASSWORD_FILE} does not
	 *                                   exist
	 */
	private Set<String> getAllUsers() throws FileNotFoundException, IOException {
		File pwdFile = new File(PASSWORD_FILE);
		Set<String> users = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(pwdFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(":", 2);
				users.add(fields[0]);
			}
		}
		return users;
	}
}
