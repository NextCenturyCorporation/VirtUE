package com.ncc.savior.virtueadmin.infrastructure;

import java.io.File;

import com.ncc.savior.virtueadmin.util.SshUtil;

/**
 * {@link IKeyManager} implementation that reads files from a directory. The
 * default will look for pem files with the filename of '<key>.pem'
 */
public class DirectoryKeyManager implements IKeyManager {

	private File directory;
	private String defaultExtension = ".pem";

	/**
	 * 
	 * @param directory
	 *            - directory where key files are stored.
	 */
	public DirectoryKeyManager(File directory) {
		this.directory = directory;
	}

	@Override
	public String getKeyByName(String keyName) {
		File file = getKeyFileByName(keyName);
		String key = SshUtil.getKeyFromFile(file);
		return key;
	}

	@Override
	public File getKeyFileByName(String keyName) {
		File file = new File(directory, keyName + defaultExtension);
		return file;
	}
}
