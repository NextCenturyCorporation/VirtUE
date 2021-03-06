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
package com.ncc.savior.prefs.file;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PreferencesFactory implementation that stores the preferences in a
 * user-defined file. To use it, set the system property
 * <tt>java.util.prefs.PreferencesFactory</tt> to
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 * <p/>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the
 * system property <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferencesFactory.java 282 2009-06-18 17:05:18Z david $
 * 
 *          Original put into public domain and modified. Original found at:
 *          http://www.davidc.net/programming/java/java-preferences-using-file-backing-store
 */
public class FilePreferencesFactory implements PreferencesFactory {
	private static final Logger logger = LoggerFactory.getLogger(FilePreferencesFactory.class);

	Preferences rootPreferences;
	public static final String SYSTEM_PROPERTY_FILE = "com.ncc.savior.FilePreferencesFactory.file";

	@Override
	public Preferences systemRoot() {
		return userRoot();
	}

	@Override
	public Preferences userRoot() {
		if (rootPreferences == null) {
			logger.debug("Instantiating root preferences");

			rootPreferences = new FilePreferences(null, "");
		}
		return rootPreferences;
	}

	private static File preferencesFile;

	public static File getPreferencesFile() {
		if (preferencesFile == null) {
			String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
			if (prefsFile == null || prefsFile.length() == 0) {
				prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs";
			}
			preferencesFile = new File(prefsFile).getAbsoluteFile();
			logger.debug("Preferences file is " + preferencesFile);
		}
		return preferencesFile;
	}

	public static void main(String[] args) throws BackingStoreException {
		System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
		System.setProperty(SYSTEM_PROPERTY_FILE, "myprefs.txt");

		Preferences p = Preferences.userNodeForPackage(FilePreferencesFactory.class);

		for (String s : p.keys()) {
			System.out.println("p[" + s + "]=" + p.get(s, null));
		}

		p.putBoolean("hi", true);
		p.put("Number", String.valueOf(System.currentTimeMillis()));
	}
}