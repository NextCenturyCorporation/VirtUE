package com.nextcentury.savior.cifsproxy.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.nextcentury.savior.cifsproxy.ActiveDirectorySecurityConfig;
import com.nextcentury.savior.cifsproxy.BaseSecurityConfig;
import com.nextcentury.savior.cifsproxy.DelegatingAuthenticationManager;
import com.nextcentury.savior.cifsproxy.GssApi;
import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.model.FileShare.SharePermissions;
import com.nextcentury.savior.cifsproxy.model.FileShare.ShareType;

@Service
@PropertySources({ @PropertySource(BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_CLASSPATH),
		@PropertySource(value = BaseSecurityConfig.DEFAULT_CIFS_PROXY_SECURITY_PROPERTIES_WORKING_DIR, ignoreResourceNotFound = true) })
public class ShareService {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(ShareService.class);

	/** path to the mount command */
	private static final String MOUNT_COMMAND = "/bin/mount";

	/**
	 * Path to the unmount command
	 */
	private static final String UNMOUNT_COMMAND = "/bin/umount";

	/**
	 * Time until we assume mount has failed (in seconds)
	 */
	private static final long MOUNT_TIMEOUT = 60;

	/**
	 * The magic Kerberos environment variable telling it which credentials cache to
	 * use (e.g., which file)
	 */
	private static final String KERBEROS_CCACHE_ENV_VAR = "KRB5CCNAME";

	/**
	 * Can only operate on the default credentials for the mounter
	 * ({@link #mountUser}) account by one thread at a time, so use this to enforce
	 * that.
	 */
	private static final Object MOUNT_CREDENTIALS_LOCK = new Object();

	private static final String MOUNT_QUERY_COMMAND = "findmnt";
	private static final String[] MOUNT_QUERY_COMMAND_ARGS = { "--json", "--canonicalize", "--types", "cifs",
			"--nofsroot" };

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

	/**
	 * The directory where the Samba config files live (e.g., smb.conf).
	 */
	@Value("${sambaConfigDir:/etc/samba")
	protected String sambaConfigDir;

	/**
	 * The relative path under {@link #sambaConfigDir} where individual share config
	 * files live.
	 */
	@Value("${virtueSharesConfigDir:virtue-shares}")
	protected String virtueSharesConfigDir;

	/**
	 * The helper shell program that creates the "virtue-shares.conf" file used as
	 * part of the samba config.
	 */
	@Value("${sambaConfigHelper:make-virtue-shares.sh")
	protected String SAMBA_CONFIG_HELPER;

	/**
	 * Tracks what files shares are mounted and their mount points.
	 */
	Map<FileShare, String> mountPoints = new ConcurrentHashMap<>();

	/**
	 * Maps file share names to their shares (for lookup by name).
	 */
	Map<String, FileShare> sharesByName = new ConcurrentHashMap<>();

	/**
	 * Ensure that the root mountpoint exists.
	 * 
	 * @see #MOUNT_ROOT
	 */
	@PostConstruct
	private void createRequiredDirectories() {
		LOGGER.entry();
		File mountRoot = new File(MOUNT_ROOT);
		if (!mountRoot.exists()) {
			LOGGER.trace("making directory: " + MOUNT_ROOT);
			if (!mountRoot.mkdirs()) {
				UncheckedIOException wse = new UncheckedIOException("could not create mount directory: " + MOUNT_ROOT,
						new IOException());
				LOGGER.throwing(wse);
				throw wse;
			}
		}
		File configDir = new File(sambaConfigDir, virtueSharesConfigDir);
		if (!configDir.exists()) {
			LOGGER.trace("making directory " + configDir);
			if (!configDir.mkdirs()) {
				UncheckedIOException wse = new UncheckedIOException("could not create mount directory: " + configDir,
						new IOException());
				LOGGER.throwing(wse);
				throw wse;
			}
		}
		LOGGER.exit();
	}

	/**
	 * Get all currently mounted shares.
	 * 
	 * @return all currently mounted shares
	 */
	public Set<FileShare> getShares() {
		LOGGER.entry();
		Set<FileShare> shares = mountPoints.keySet();
		LOGGER.exit(shares);
		return shares;
	}

	/**
	 * Mount a new share. Its name must be different from all currently mounted
	 * shares.
	 * 
	 * @param session
	 *                    the current http session
	 * @param share
	 *                    the new share
	 * @throws IllegalArgumentException
	 *                                      if permissions are empty or the share is
	 *                                      already mounted
	 * @throws IOException
	 *                                      if there was an error creating config
	 *                                      files
	 */
	public void newShare(HttpSession session, FileShare share) throws IllegalArgumentException, IOException {
		LOGGER.entry(session, share);
		Set<SharePermissions> permissions = share.getPermissions();
		if (permissions.isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("permissions cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (mountPoints.containsKey(share)) {
			IllegalArgumentException e = new IllegalArgumentException(
					"share '" + share.getName() + "' already mounted");
			LOGGER.throwing(e);
			throw e;
		}
		mountShare(session, share);
		exportShare(share);
		LOGGER.exit();
	}

	/**
	 * Configure Samba to export the file share.
	 * 
	 * @param share
	 *                  file share to export
	 * @throws IOException
	 */
	private void exportShare(FileShare share) throws IOException {
		File configDir = new File(sambaConfigDir, virtueSharesConfigDir);
		File virtueConfigDir = new File(configDir, share.getVirtue());
		if (!virtueConfigDir.mkdirs()) {
			throw new IOException("could not create config dir: " + virtueConfigDir);
		}
		FileWriter configWriter = new FileWriter(new File(virtueConfigDir, share.getName() + ".conf"));
		configWriter.write(makeShareConfig(share));
		configWriter.close();
		Runtime.getRuntime().exec(SAMBA_CONFIG_HELPER);
	}

	private String makeShareConfig(FileShare share) {
		String mountPoint = getMountPoint(share);
		File path = new File(MOUNT_ROOT, mountPoint);

		StringBuilder config = new StringBuilder(
				"[" + share.getName() + "]\n" + "path = " + path + "\n" + "valid users = " + share.getVirtue() + "\n");
		// TODO someday add "hosts allow = " when we can get info about which hosts will
		// be connecting
		if (!share.getPermissions().contains(FileShare.SharePermissions.WRITE)) {
			config.append("read only = yes");
		}
		return config.toString();
	}

	/**
	 * Perform the Kerberos gyrations required to authenticate the mount process and
	 * do the mounting.
	 * 
	 * @param session
	 *                    the current session
	 * @param share
	 *                    the share to mount
	 */
	private void mountShare(HttpSession session, FileShare share) {
		LOGGER.entry(session, share);
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
			getServiceTicket(intermediateCCacheFilename, username, share.getServer(), keytabFilename);
			intermediateCCache.toFile().setReadable(true, false);
			// only one at a time can muck with default user credentials (of mountUser)
			synchronized (MOUNT_CREDENTIALS_LOCK) {
				switchPrincipal(intermediateCCacheFilename, mountUser, username);
				doMount(share, username);
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
		LOGGER.exit();
	}

	private String getMountPoint(FileShare fs) {
		LOGGER.entry(fs);
		String mountPoint = mountPoints.get(fs);
		if (mountPoint == null) {
			mountPoint = fs.getName();
			mountPoints.put(fs, mountPoint);
		}
		LOGGER.exit(mountPoint);
		return mountPoint;
	}

	/**
	 * Initialize a credential cache with our service credential. Currently uses
	 * <code>kinit</code> but ideally we'd do this with {@link GssApi} calls.
	 * 
	 * @param cCacheFilename
	 *                           the file to initialize
	 * @param keytabFilename
	 *                           the keytab to initialize with. Must contain the key
	 *                           for the service (see
	 *                           {@link DelegatingAuthenticationManager#getServiceName(char)}).
	 */
	private void initCCache(String cCacheFilename, String keytabFilename) {
		LOGGER.entry(cCacheFilename, keytabFilename);
		String serviceName = DelegatingAuthenticationManager.getServiceName('/');
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
	private void importCredentials(String toCCacheFilename, String fromCCacheFilename) {
		LOGGER.entry(toCCacheFilename, fromCCacheFilename);
		ProcessBuilder processBuilder = createProcessBuilder(toCCacheFilename);
		processBuilder.command("importcreds", fromCCacheFilename);
		runProcess(processBuilder, "importcreds");
		extraTracing(toCCacheFilename);
		LOGGER.exit();
	}

	/**
	 * Get a proxy service ticket for a file server and a specific user. Currently
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
	private void switchPrincipal(String ccacheFilename, String mountUser, String username) {
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
	private void extraTracing(String ccacheFile) {
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
	 * @see #KERBEROS_CCACHE_ENV_VAR
	 */
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
	private void doMount(FileShare share, String username) throws WebServiceException {
		LOGGER.entry(share, username);
		String mountPoint = getMountPoint(share);
		File destination = new File(MOUNT_ROOT, mountPoint);
		// make sure the destination is inside MOUNT_ROOT
		String canonicalDest;
		try {
			canonicalDest = destination.getCanonicalPath();
			LOGGER.debug("canonical mount path: " + canonicalDest);
			if (!canonicalDest.substring(0, MOUNT_ROOT.length()).equals(MOUNT_ROOT)
					|| canonicalDest.charAt(MOUNT_ROOT.length()) != File.separatorChar) {
				WebServiceException wse = new WebServiceException(
						"invalid mountpoint '" + mountPoint + "' for share '" + share.getName() + "'");

				LOGGER.throwing(wse);
				throw wse;
			}
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException(
					"could not create full path for mount path: " + mountPoint, e);
			LOGGER.throwing(wse);
			throw wse;
		}
		if (!destination.exists() && !destination.mkdir()) {
			WebServiceException wse = new WebServiceException(
					"could not create target mountpoint '" + mountPoint + "' for share '" + share.getName() + "'");
			LOGGER.throwing(wse);
			throw wse;
		}
		String sourcePath = share.getPath();
		// assemble arguments for mounting cifs
		if (!sourcePath.startsWith("/")) {
			sourcePath = "/" + sourcePath;
		}
		String simpleUsername = username.split("@")[0];

		boolean readOnly = !share.getPermissions().contains(SharePermissions.WRITE);
		// The "vers=3.0" may help avoid hangs that sometimes occur with mount.cifs. See
		// https://askubuntu.com/questions/752398/mount-cifs-hangs-and-becomes-unresponsive
		String options = "username=" + simpleUsername + (readOnly ? ",ro" : "") + ",sec=krb5,vers=3.0,cruid="
				+ mountUser;
		String[] args = { "sudo", MOUNT_COMMAND, "-t", "cifs", "//" + share.getServer() + sourcePath, canonicalDest,
				"-v", "-o", options };
		LOGGER.debug("mount command: " + Arrays.toString(args));
		ProcessBuilder processBuilder = createProcessBuilder(null);
		processBuilder.command(args);
		runProcess(processBuilder, "mount");
		addInternal(share, canonicalDest);
		LOGGER.exit();
	}

	private void addInternal(FileShare share, String canonicalDest) {
		LOGGER.entry(share, canonicalDest);
		mountPoints.put(share, canonicalDest);
		sharesByName.put(share.getName(), share);
		LOGGER.exit();
	}

	/**
	 * Look up a file share by name
	 * 
	 * @param name
	 *                 the name of the share to get
	 * @return the share named <code>name</code>
	 * 
	 */
	public FileShare getShare(String name) {
		LOGGER.entry(name);
		FileShare share = sharesByName.get(name);
		LOGGER.exit(share);
		return share;
	}

	/**
	 * Remove and unmount the share with the given name
	 * 
	 * @param name
	 *                 the name of the share to unmount
	 * 
	 * @throws IllegalArgumentException
	 *                                      if the named share is not mounted
	 */
	public void removeShare(String name) throws IllegalArgumentException {
		LOGGER.entry(name);
		FileShare share = sharesByName.get(name);
		if (share == null) {
			IllegalArgumentException e = new IllegalArgumentException("share '" + name + "' is not mounted");
			LOGGER.throwing(e);
			throw e;
		}
		unmountShare(share);
		mountPoints.remove(share);
		sharesByName.remove(name);
		LOGGER.exit();
	}

	/**
	 * Unmount a share
	 * 
	 * @param share
	 *                  the share to unmount
	 */
	private void unmountShare(FileShare share) {
		LOGGER.entry(share);
		ProcessBuilder processBuilder = createProcessBuilder(null);
		List<String> command = new ArrayList<>();
		command.add("sudo");
		command.add(UNMOUNT_COMMAND);
		command.add("--lazy");
		command.add(getMountPoint(share));
		processBuilder.command(command);
		runProcess(processBuilder, "unmount " + share.getName());
		LOGGER.exit();
	}

	/** Corresponds to output of {@link ShareService#MOUNT_QUERY_COMMAND}. */
	protected static class Filesystems {
		public List<Target> filesystems;
	}

	protected static class Target {
		public String target;
		public String source;
		public String fstype;
		public String options;
	}

	public void scan() throws IOException {
		LOGGER.entry();
		mountPoints.clear();
		sharesByName.clear();

		List<String> command = new ArrayList<>();
		command.add(MOUNT_QUERY_COMMAND);
		command.addAll(Arrays.asList(MOUNT_QUERY_COMMAND_ARGS));
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectOutput(Redirect.PIPE);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"running mount query: " + MOUNT_QUERY_COMMAND + " " + String.join(" ", MOUNT_QUERY_COMMAND_ARGS));
		}
		Process queryProcess = processBuilder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(queryProcess.getInputStream()));
		StringJoiner sj = new StringJoiner("\n");
		reader.lines().iterator().forEachRemaining(sj::add);
		String queryOutput = sj.toString();
		LOGGER.debug("query output: " + queryOutput);

		if (queryOutput.length() > 0) {
			MappingJsonFactory mappingJsonFactory = new MappingJsonFactory();
			JsonParser parser = mappingJsonFactory.createParser(queryOutput);
			Filesystems filesystems = parser.readValueAs(Filesystems.class);
			if (filesystems != null) {
				String prefix = MOUNT_ROOT + "/";
				Pattern sourcePattern = Pattern.compile("//([^/]*)(/.*)");
				for (Target target : filesystems.filesystems) {
					try {
						if (target.target.startsWith(prefix)) {
							// we've got a filesystem we should manage
							String relativePath = target.target.substring(MOUNT_ROOT.length() + 1);
							String virtue = relativePath.split(File.separator)[0];
							if (!virtue.isEmpty()) {
								Matcher sourceMatcher = sourcePattern.matcher(target.source);
								if (sourceMatcher.matches()) {
									String server = sourceMatcher.group(1);
									String path = sourceMatcher.group(2);
									String name = target.target.substring(MOUNT_ROOT.length() + 1);
									List<String> options = Arrays.asList(target.options.split(","));
									Set<SharePermissions> permissions = new HashSet<FileShare.SharePermissions>();
									permissions.add(SharePermissions.READ);
									if (options.contains("ro")) {
									} else if (options.contains("rw")) {
										permissions.add(SharePermissions.WRITE);
									} else {
										LOGGER.warn("Options contain neither 'ro' nor 'rw' for file share: " + name);
									}
									FileShare fileShare = new FileShare(name, virtue, server, path, permissions,
											ShareType.CIFS);
									addInternal(fileShare, target.target);
								} else {
									LOGGER.warn("cannot parse source for CIFS filesystem: " + target.source);
								}
							} else {
								LOGGER.warn("cannot get Virtue for mount point: " + target.target);
							}
						}
					} catch (RuntimeException e) {
						LOGGER.warn("got exception while processing mount point '" + target.target + "': " + e);
					}
				}
			}
		}

		LOGGER.exit();
	}

	public static void main(String[] args) throws IOException {
		ShareService shareService = new ShareService();
		shareService.MOUNT_ROOT = "/mnt/cifs-proxy";

		shareService.scan();
		Set<FileShare> shares = shareService.getShares();
		System.out.println("Found " + shares.size() + " shares:");
		for (FileShare fileShare : shares) {
			System.out.println(fileShare);
		}
	}
}
