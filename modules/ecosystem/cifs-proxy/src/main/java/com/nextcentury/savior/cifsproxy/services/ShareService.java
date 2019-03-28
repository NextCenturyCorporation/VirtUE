/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.nextcentury.savior.cifsproxy.services;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.nextcentury.savior.cifsproxy.ActiveDirectorySecurityConfig;
import com.nextcentury.savior.cifsproxy.BaseSecurityConfig;
import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.model.FileShare.SharePermissions;
import com.nextcentury.savior.cifsproxy.model.FileShare.ShareType;
import com.nextcentury.savior.cifsproxy.model.Virtue;

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
	 * Can only operate on the default credentials for the mounter
	 * ({@link #mountUser}) account by one thread at a time, so use this to enforce
	 * that.
	 */
	private static final Object MOUNT_CREDENTIALS_LOCK = new Object();

	private static final String MOUNT_QUERY_COMMAND = "findmnt";
	private static final String[] MOUNT_QUERY_COMMAND_ARGS = { "--json", "--canonicalize", "--types", "cifs",
			"--nofsroot" };

	/** where to mount files for the Virtue */
	@Value("${savior.cifsproxy.mountRoot:/mnt/cifs-proxy}")
	public String MOUNT_ROOT;

	/**
	 * The user whose credentials will be set and used to do the mount (because
	 * mount.cifs(8) can only use default user credentials and ignores
	 * {@value KerberosUtils#KERBEROS_CCACHE_ENV_VAR}).
	 */
	@Value("${savior.cifsproxy.mountUser:mounter}")
	public String mountUser;

	/**
	 * Required: Active Directory security domain.
	 */
	@Value("${savior.security.ad.domain}")
	private String adDomain;

	@Autowired
	private SambaConfigManager sambaConfigManager;
	
	@Autowired
	private VirtueService virtueService;

	/**
	 * Tracks what files shares are mounted and their mount points (relative to
	 * {@link #MOUNT_ROOT}).
	 */
	protected Map<FileShare, String> mountPoints = new ConcurrentHashMap<>();

	/**
	 * Maps file share names to their shares (for lookup by name).
	 */
	protected Map<String, FileShare> sharesByName = new ConcurrentHashMap<>();

	/**
	 * Ensure that the root mountpoint exists.
	 * 
	 * @see #MOUNT_ROOT
	 */
	@PostConstruct
	private void createMountDirectory() {
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
		if (share.getName() == null || share.getName().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("name cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (share.getVirtueId() == null || share.getVirtueId().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("virtueId cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (share.getServer() == null || share.getServer().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("server cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
		if (share.getPath() == null || share.getPath().isEmpty()) {
			IllegalArgumentException e = new IllegalArgumentException("path cannot be empty");
			LOGGER.throwing(e);
			throw e;
		}
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
		if (virtueService.getVirtue(share.getVirtueId()) == null) {
			IllegalArgumentException e = new IllegalArgumentException("unknown Virtue '" + share.getVirtueId() + "'");
			LOGGER.throwing(e);
			throw e;
		}
		sambaConfigManager.initExportedName(share);
		sharesByName.put(share.getName(), share);
		try {
			mountShare(session, share);
			sambaConfigManager.writeShareConfig(share.getName(), share.getVirtueId(), makeShareConfig(share));
		} catch (IOException | RuntimeException e) {
			// undo the mount and clean up
			try {
				unmountShare(share);
			} catch (Throwable t) {
				LOGGER.info("error unmounting after an error with share: " + share);
			}
			mountPoints.remove(share);
			sharesByName.remove(share.getName());
			LOGGER.throwing(e);
			throw e;
		}
		LOGGER.exit();
	}

	private String makeShareConfig(FileShare share) {
		LOGGER.entry(share);
		String mountPoint = getMountPoint(share);
		File path = new File(MOUNT_ROOT, mountPoint);

		Virtue virtue = virtueService.getVirtue(share.getVirtueId());
		StringBuilder config = new StringBuilder("[" + share.getName() + "]\n" + "path = " + path.getAbsolutePath()
				+ "\n" + "valid users = " + virtue.getUsername() + "\n");
		// TODO someday add "hosts allow = " when we can get info about which hosts will
		// be connecting
		String readOnly = share.getPermissions().contains(FileShare.SharePermissions.WRITE) ? "no" : "yes";
		config.append("read only = " + readOnly + "\n");

		LOGGER.exit(config.toString());
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
			KerberosUtils.initCCache(intermediateCCacheFilename, keytabFilename, adDomain);
			KerberosUtils.importCredentials(intermediateCCacheFilename, ccacheFilename);
			KerberosUtils.getServiceTicket(intermediateCCacheFilename, username, share.getServer(), keytabFilename);
			intermediateCCache.toFile().setReadable(true, false);
			// only one at a time can muck with default user credentials (of mountUser)
			synchronized (MOUNT_CREDENTIALS_LOCK) {
				KerberosUtils.switchPrincipal(intermediateCCacheFilename, mountUser, username);
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

	/**
	 * Get the relative mount point for a file share. Stores the value in
	 * {@link #mountPoints} if it was not already present.
	 *
	 * @param fs
	 * @return relative mount point for the file system
	 * @see #MOUNT_ROOT
	 */
	private String getMountPoint(FileShare fs) {
		LOGGER.entry(fs);
		String mountPoint = mountPoints.get(fs);
		if (mountPoint == null) {
			mountPoint = SambaConfigManager.sanitizeFilename(fs.getVirtueId()) + File.separator + fs.getExportedName();
			mountPoints.put(fs, mountPoint);
		}
		LOGGER.exit(mountPoint);
		return mountPoint;
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
		File destination = new File(MOUNT_ROOT + File.separator + mountPoint);
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
		if (!destination.exists() && !destination.mkdirs()) {
			WebServiceException wse = new WebServiceException(
					"could not create target mountpoint '" + destination + "' for share '" + share.getName() + "'");
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
		String localUser = virtueService.getVirtue(share.getVirtueId()).getUsername();
		// The "vers=3.0" may help avoid hangs that sometimes occur with mount.cifs. See
		// https://askubuntu.com/questions/752398/mount-cifs-hangs-and-becomes-unresponsive
		// Setting a non-root user (uid) and file_mode and dir_mode both to 0777 seems
		// to be required for remounts to be writable.
		String options = "username=" + simpleUsername + (readOnly ? ",ro" : "") + ",sec=krb5,vers=3.0,cruid="
				+ mountUser + ",uid=" + localUser + ",file_mode=0777,dir_mode=0777";
		String[] args = { "sudo", MOUNT_COMMAND, "-t", "cifs", "//" + share.getServer() + sourcePath, canonicalDest,
				"-v", "-o", options };
		LOGGER.debug("mount command: " + Arrays.toString(args));
		ProcessBuilder processBuilder = KerberosUtils.createProcessBuilder(null);
		processBuilder.command(args);
		KerberosUtils.runProcess(processBuilder, "mount");
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
	 * @throws IOException
	 *                                      if the Samba config file for the share
	 *                                      cannot be deleted
	 */
	public void removeShare(String name) throws IllegalArgumentException, IOException {
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
	 * @throws IOException
	 *                         if the Samba config file for the share exists and
	 *                         cannot be deleted
	 */
	private void unmountShare(FileShare share) throws IOException {
		LOGGER.entry(share);
		ProcessBuilder processBuilder = KerberosUtils.createProcessBuilder(null);
		List<String> command = new ArrayList<>();
		command.add("sudo");
		command.add(UNMOUNT_COMMAND);
		command.add("--lazy");
		String mountPoint = MOUNT_ROOT + File.separator + getMountPoint(share);
		LOGGER.debug("Unmounting '" + mountPoint + "'");
		command.add(mountPoint);
		processBuilder.command(command);
		LOGGER.trace("Running unmount: " + String.join(" ", command));
		KerberosUtils.runProcess(processBuilder, "unmount " + share.getName());
		sambaConfigManager.removeConfigFile(share.getName(), share.getVirtueId());
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

		StringJoiner sj = new StringJoiner("\n");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(queryProcess.getInputStream()))) {
			reader.lines().iterator().forEachRemaining(sj::add);
		}
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
							String[] targetPath = relativePath.split(Pattern.quote(File.separator));
							String virtue = targetPath[0];
							if (!virtue.isEmpty()) {
								Matcher sourceMatcher = sourcePattern.matcher(target.source);
								if (sourceMatcher.matches()) {
									String server = sourceMatcher.group(1);
									String path = sourceMatcher.group(2);
									String name = targetPath[targetPath.length - 1];
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

									sharesByName.put(fileShare.getName(), fileShare);
									getMountPoint(fileShare);
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
