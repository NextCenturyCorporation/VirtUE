package com.ncc.savior.desktop.sidebar.prefs;

/**
 * Contains path, key and value for preference. This data is generally not user
 * friendly, but the values used by the Desktop Application to use the
 * preferences.
 *
 */
public class DesktopPreferenceData {
	public String pathFromUserRoot;
	public String key;
	public String value;

	public DesktopPreferenceData(String pathFromUserRoot, String key, String value) {
		super();
		this.pathFromUserRoot = pathFromUserRoot;
		this.key = key;
		this.value = value;
		if (pathFromUserRoot.indexOf("/") == 0) {
			this.pathFromUserRoot = pathFromUserRoot.substring(1);
		}
	}

	public String getPathFromUserRoot() {
		return pathFromUserRoot;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}